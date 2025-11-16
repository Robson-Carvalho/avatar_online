// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract MatchResult {
    struct Match {
        string player1;
        string player2;
        string winner;
        uint256 timestamp;
    }

    Match[] public matches;

    event MatchRecorded(
        uint256 matchId,
        string player1,
        string player2,
        string winner,
        uint256 timestamp
    );

    function recordMatch(
        string memory player1,
        string memory player2,
        string memory winner
    ) public {
        matches.push(Match(player1, player2, winner, block.timestamp));

        uint256 id = matches.length - 1;

        emit MatchRecorded(id, player1, player2, winner, block.timestamp);
    }

    function totalMatches() public view returns (uint256) {
        return matches.length;
    }
}
