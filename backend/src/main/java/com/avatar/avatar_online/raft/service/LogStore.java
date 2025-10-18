package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.raft.model.LogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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

    private final CommandExecutorService commandExecutorService;

    @Autowired
    public LogStore(LogConsensusService logConsensusService, CommandExecutorService commandExecutorService) {
        this.logConsensusService = logConsensusService;
        this.commandExecutorService = commandExecutorService;
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
        logConsensusService.updateLastLogIndex(entry.getIndex());
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

    public void markCommitted(long newCommitIndex) {
        long oldCommitIndex = lastCommittedIndex.get();

        if (newCommitIndex > oldCommitIndex && newCommitIndex <= lastLogIndex.get()) {


            for (long i = oldCommitIndex + 1; i <= newCommitIndex; i++) {
                LogEntry entry = logEntries.get(i);
                if (entry != null) {
                    commandExecutorService.executeCommand(entry);
                }
            }

            lastCommittedIndex.set(newCommitIndex);

            logConsensusService.updateLastCommittedIndex(newCommitIndex);

            System.out.println("✅ Log commitado e aplicado até o índice: " + newCommitIndex);
        }
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

    public void tryAdvanceCommitIndex(long currentTerm, long lastLogIndex) {
        List<Long> indices = new ArrayList<>(logConsensusService.getMatchIndexValues());

        indices.add(lastLogIndex);

        Collections.sort(indices);

        int majorityIndex = indices.size() / 2;
        long newCommitIndex = indices.get(majorityIndex);

        long currentCommitIndex = getLastCommitIndex();

        if (newCommitIndex > currentCommitIndex && getTermOfIndex(newCommitIndex) == currentTerm) {
            markCommitted(newCommitIndex);
        }
    }
}