package com.avatar.avatar_online.game;

import com.avatar.avatar_online.publisher_subscriber.handlers.DTO.MatchFoundResponseDTO;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MatchManagementService {
    private final IMap<String, Match> activeMatchesMap;

    private static final String ACTIVE_MATCHES_MAP = "active-matches";

    public MatchManagementService(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast) {
        this.activeMatchesMap = hazelcast.getMap(ACTIVE_MATCHES_MAP);
    }

    public int gamesRunning(){
        return activeMatchesMap.size();
    }

    public String getOpponentIfPlayerInMatch(String sessionId, String userID) {
        for (Match match : activeMatchesMap.values()) {

            if (sessionId.equals(match.getPlayer1().getUserSession()) || userID.equals(match.getPlayer1().getUserId())) {
                return match.getPlayer2().getUserSession();
            }else if(sessionId.equals(match.getPlayer2().getUserSession()) || userID.equals(match.getPlayer2().getUserId())){
                return match.getPlayer1().getUserSession();
            }
        }

        return "";
    }

    /**
     * Registra uma nova partida no cluster, definindo o Gerente da Partida (MP) e as conexões dos jogadores.
     * Deve ser chamado tipicamente pelo nó Líder após um Matchmaking bem-sucedido.
     *
     * @param match O objeto MatchState contendo todas as informações de roteamento.
     */
    public void registerMatch(Match match) {
        activeMatchesMap.put(match.getMatchId(), match);

        System.out.println("Partida registrada no cluster: " + match.getMatchId() +
                ", MP: ");
    }

    /**
     * Obtém o estado de roteamento de uma partida.
     *
     * @param matchId O ID da partida.
     * @return O MatchState, ou null se a partida não for encontrada.
     */
    public Match getMatchState(String matchId) {
        return activeMatchesMap.get(matchId);
    }

    /**
     * Remove uma partida do cluster, geralmente após o seu término.
     *
     * @param matchId O ID da partida a ser removida.
     */
    public void unregisterMatch(String matchId) {
        activeMatchesMap.remove(matchId);
        System.out.println("Partida desregistrada do cluster: " + matchId);
    }

    /**
     * Verifica se o nó local é o Gerente da Partida (MP) para o MatchId fornecido.
     *
     * @param matchId O ID da partida.
     * @param currentNodeId O UUID do nó local.
     * @return true se o nó local for o MP, false caso contrário.
     */
    public boolean isMatchManager(String matchId, String currentNodeId) {
        Match state = getMatchState(matchId);
        if (state == null) {
            return false;
        }
        return state.getManagerNodeId().equals(currentNodeId);
    }
}
