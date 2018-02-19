package nl.tue.s2id90.group50.own_bots;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import nl.tue.s2id90.group50.AIStoppedException;
import nl.tue.s2id90.group50.DraughtsNode;
import org10x10.dam.game.Move;

/**
 * Implementation of the DraughtsPlayer interface.
 *
 * @author huub
 */
// ToDo: rename this class (and hence this file) to have a distinct name
//       for your player during the tournament
public class ImminentDeath extends DraughtsPlayer {

    Random random = new Random();
    static final int PRECISION = 5;
    
    private int bestValue = 0;
    int maxSearchDepth;
    int visitedStates;

    /**
     * boolean that indicates that the GUI asked the player to stop thinking.
     */
    private boolean stopped;

    public ImminentDeath() {
        super("Pain.jpg"); // ToDo: replace with your own icon
    }

    @Override
    public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        maxSearchDepth = 0;
        DraughtsNode node = new DraughtsNode(s);    // the root of the search tree
        try {
            while (!stopped) {
                visitedStates = 0;
                maxSearchDepth++;
                // compute bestMove and bestValue in a call to alphabeta
                bestValue = alphaBeta(node, MIN_VALUE + 2 * PRECISION, MAX_VALUE - 2 * PRECISION, maxSearchDepth);

                // store the bestMove found uptill now
                // NB this is not done in case of an AIStoppedException in alphaBeat()
                bestMove = node.getBestMove();
            }
        } catch (AIStoppedException ex) { /* nothing to do */ }
        if (bestMove == null) {
            System.err.println("no valid move found!");
            return getRandomValidMove(s);
        } else {
            // print the results for debugging reasons
            System.err.format(
                        "%s: depth = %2d, best move = %5s, value = %d\n, discovered = %8d,",
                        this.getClass().getSimpleName(), maxSearchDepth - 1, bestMove, bestValue, visitedStates
                );
            return bestMove;
        }
    }

    /**
     * This method's return value is displayed in the AICompetition GUI.
     *
     * @return the value for the draughts state s as it is computed in a call to getMove(s).
     */
    @Override
    public Integer getValue() {
        return bestValue;
    }

    /**
     * Tries to make alphabeta search stop. Search should be implemented such that it throws an AIStoppedException when
     * boolean stopped is set to true;
     */
    @Override
    public void stop() {
        stopped = true;
    }

    /**
     * returns random valid move in state s, or null if no moves exist.
     */
    Move getRandomValidMove(DraughtsState s) {
        List<Move> moves = s.getMoves();
        Collections.shuffle(moves);
        return moves.isEmpty() ? null : moves.get(0);
    }

    /**
     * Implementation of alphabeta that automatically chooses the white player as maximizing player and the black player
     * as minimizing player.
     *
     * @param node contains DraughtsState and has field to which the best move can be assigned.
     * @param alpha
     * @param beta
     * @param depth maximum recursion Depth
     * @return the computed value of this node
     * @throws AIStoppedException
     *
     */
    int alphaBeta(DraughtsNode node, int alpha, int beta, int depth) throws AIStoppedException {
        if (node.getState().isWhiteToMove()) {
            return alphaBetaMax(node, alpha, beta, depth);
        } else {
            return alphaBetaMin(node, alpha, beta, depth);
        }
    }

    /**
     * Does an alphabeta computation with the given alpha and beta where the player that is to move in node is the
     * minimizing player.
     *
     * <p>
     * Typical pieces of code used in this method are:
     * <ul> <li><code>DraughtsState state = node.getState()</code>.</li>
     * <li><code> state.doMove(move); .... ; state.undoMove(move);</code></li>
     * <li><code>node.setBestMove(bestMove);</code></li>
     * <li><code>if(stopped) { stopped=false; throw new AIStoppedException(); }</code></li>
     * </ul>
     * </p>
     *
     * @param node contains DraughtsState and has field to which the best move can be assigned.
     * @param alpha
     * @param beta
     * @param depth maximum recursion Depth
     * @return the compute value of this node
     * @throws AIStoppedException thrown whenever the boolean stopped has been set to true.
     */
    int alphaBetaMin(DraughtsNode node, int alpha, int beta, int depth) throws AIStoppedException {
        if (stopped) {
            stopped = false;
            throw new AIStoppedException();
        }
        visitedStates++;
        DraughtsState state = node.getState();
        if (state.isWhiteToMove()) { throw new Error(); }
        List<Move> possibleMoves = state.getMoves();
        if (state.isEndState()) {
            return evaluate(state);
        }
        Move bestMove = possibleMoves.get(0);
        if (depth < 0 && !bestMove.isCapture()) {
            return evaluate(state);
        }
        ArrayList<Move> goodMoves = new ArrayList<>();
        goodMoves.add(bestMove);
        int foundBeta;
        for (Move move : possibleMoves) {
            state.doMove(move);
            foundBeta = alphaBetaMax(new DraughtsNode(state), alpha, beta, depth - 1);
            state.undoMove(move);
            if (beta + PRECISION > foundBeta) {
                goodMoves.add(move);
            }
            if (beta > foundBeta) {
                goodMoves.clear();
                goodMoves.add(move);
                beta = foundBeta;
                if (beta <= alpha) {
                    return alpha;
                }
            }
        }
        Collections.shuffle(goodMoves);
        node.setBestMove(goodMoves.get(0));
        return beta;
    }

    int alphaBetaMax(DraughtsNode node, int alpha, int beta, int depth) throws AIStoppedException {
        if (stopped) {
            stopped = false;
            throw new AIStoppedException();
        }
        visitedStates++;
        DraughtsState state = node.getState();
        if (!state.isWhiteToMove()) { throw new Error(); }
        List<Move> possibleMoves = state.getMoves();
        if (state.isEndState()) {
            return evaluate(state);
        }
        Move bestMove = possibleMoves.get(0);
        if (depth < 0 && !bestMove.isCapture()) {
            return evaluate(state);
        }
        ArrayList<Move> goodMoves = new ArrayList<>();
        goodMoves.add(bestMove);
        int foundAlpha;     
        for (Move move : possibleMoves) {
            state.doMove(move);
            foundAlpha = alphaBetaMin(new DraughtsNode(state), alpha, beta, depth - 1);
            state.undoMove(move);
            if (alpha - PRECISION < foundAlpha) {
                goodMoves.add(move);
            }
            if (alpha < foundAlpha) {
                goodMoves.clear();
                goodMoves.add(move);
                alpha = foundAlpha;
                if (alpha >= beta) {
                    return beta;
                }  
            }
        }
        Collections.shuffle(goodMoves);
        node.setBestMove(goodMoves.get(0));
        return alpha;
    }

    /**
     * A method that evaluates the given state.
     */
    // ToDo: write an appropriate evaluation function
    int evaluate(DraughtsState state) {
        int[] pieces = state.getPieces();
        int value = 0;
        // empty = 0, whitePiece = 1, blackpiece = 2, whiteKing = 3, blackKing = 4
        // uses very simplistic evaluation by piece count.
        for (int piece : pieces) {
            if (piece == 1) {
                value += 100;
            } else if (piece == 2) {
                value -= 100;
            } else if (piece == 3) {
                value += 500;
            } else if (piece == 4) {
                value -= 500;
            }
        }
        return value;
    }
}
