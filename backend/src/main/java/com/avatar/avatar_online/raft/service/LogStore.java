package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.raft.model.LogEntry;
import com.avatar.avatar_online.service.LogConsensusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class LogStore {

    private final ConcurrentNavigableMap<Long, LogEntry> logEntries = new ConcurrentSkipListMap<>();

    private final AtomicLong lastLogIndex = new AtomicLong(0);
    private final AtomicLong lastCommittedIndex = new AtomicLong(0);

    private final AtomicLong lastLogTerm = new AtomicLong(0);

    private final LogConsensusService logConsensusService;

    @Autowired
    public LogStore(LogConsensusService logConsensusService) {
        this.logConsensusService = logConsensusService;
    }

    /**
     * Adiciona uma nova LogEntry ao final do log (apenas localmente).
     * @param entry A LogEntry a ser adicionada. O índice da entrada deve ser maior que o último índice.
     */
    public void append(LogEntry entry) {
        if (entry.getIndex() != lastLogIndex.get() + 1) {
            throw new IllegalArgumentException("Log entry index is out of order.");
        }

        logEntries.put(entry.getIndex(), entry);
        lastLogIndex.set(entry.getIndex());
        lastLogTerm.set(entry.getTerm());
    }

    /**
     * Retorna o índice da última entrada commitada neste nó.
     * @return O último índice do log, ou 0 se o log estiver vazio.
     */
    public long getLastCommitIndex() {
        return lastCommittedIndex.get();
    }

    /**
     * Retorna o índice da última entrada persistida neste nó.
     * @return O último índice do log, ou 0 se o log estiver vazio.
     */
    public long getLastIndex() {
        return lastLogIndex.get();
    }

    /**
     * Marca o log até o índice fornecido como commitado.
     * Este método TAMBÉM é responsável por notificar o cluster do novo estado commitado.
     * @param index O índice do log que acaba de ser commitado.
     */
    public void markCommitted(long index) {
        if (index > lastCommittedIndex.get() && index <= lastLogIndex.get()) {

            System.out.println("✅ Log commitado localmente até o índice: " + index +
                    " | Aplicando estado de negócio...");

            lastCommittedIndex.set(index);

            logConsensusService.updateLastCommittedIndex(index);
        }
    }

    /**
     * Retorna todas as entradas de log que foram persistidas, mas ainda não commitadas.
     * Usado para a replicação.
     * @return Uma lista de LogEntries não commitadas.
     */
    public List<LogEntry> getUncommittedEntries() {
        return logEntries.entrySet().stream()
                .filter(entry -> entry.getKey() > lastCommittedIndex.get())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Obtém uma lista de entradas a partir de um índice específico (incluindo o startIndex).
     * Usado para a replicação de log.
     */
    public List<LogEntry> getEntriesFrom(long startIndex) {
        if (startIndex == 0) {
            return logEntries.values().stream().collect(Collectors.toList());
        }
        // Usa tailMap para obter um segmento eficiente do log
        return new ArrayList<>(logEntries.tailMap(startIndex, true).values());
    }

    /**
     * Usado pelo Líder para garantir que o seu log corresponde ao do Seguidor.
     */
    public long getTermOfIndex(long index) {
        if (index == 0) return 0;
        LogEntry entry = logEntries.get(index);
        return entry != null ? entry.getTerm() : -1; // -1 indica que não existe
    }

    /**
     * Implementa a Regra de Segurança do Log (Truncate): remove entradas conflitantes
     * a partir do índice fornecido (inclusive).
     */
    public void truncateLog(long index) {
        if (index <= lastCommittedIndex.get()) {
            throw new IllegalArgumentException("Cannot truncate logs that are already committed.");
        }

        logEntries.tailMap(index, true).keySet()
                .forEach(logEntries::remove);

        if (logEntries.isEmpty()) {
            lastLogIndex.set(0);
            lastLogTerm.set(0);
        } else {
            long newLastIndex = logEntries.lastKey();
            lastLogIndex.set(newLastIndex);
            lastLogTerm.set(logEntries.get(newLastIndex).getTerm());
        }
        System.out.println("🔥 Logs truncados a partir do índice: " + index +
                ". Novo último índice: " + lastLogIndex.get());
    }
}