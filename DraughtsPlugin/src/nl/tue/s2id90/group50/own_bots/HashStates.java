/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.s2id90.group50.own_bots;

import java.util.HashMap;
import org10x10.dam.game.Move;

/**
 *
 * @author s161530
 */
public class HashStates {
    
    HashMap<String, Move> hash = new HashMap<>();
    
    void insert (String state, Move move) {
        hash.put(state, move);
    }
    
    Move Retieve(String state) {
        return hash.get(state);
    }
    
}
