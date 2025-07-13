package main.AlphaBeta.TranspositionTable;

import main.models.Move;

import java.util.concurrent.ConcurrentHashMap;

public class TranspositionTable {
    private final ConcurrentHashMap<Long, TableEntry> map = new ConcurrentHashMap<>(1<<20); // ~1 M

    public TableEntry get(long key){
        return map.get(key);
    }

    public void store(long key, int depth, int score, byte flag, Move bestMove) {
        TableEntry cur = map.get(key);
        if (cur == null || depth >= cur.depth)
            map.put(key, new TableEntry(depth, score, flag, bestMove));
    }
}