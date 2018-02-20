package nl.tue.s2id90.group50.own_bots;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.Collections;
import java.util.List;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import nl.tue.s2id90.group50.AIStoppedException;
import nl.tue.s2id90.group50.DraughtsNode;
import org10x10.dam.game.DamConstants;
import org10x10.dam.game.Move;

/**
 * Implementation of the basic required player.
 *
 * @author Jeroen, Andreas
 */
public class RandomizedAlphaBeta extends DraughtsPlayer implements DamConstants {
    
    private final static int MAXSEARCHDEPTH = 200;
    private final static int RANDOMSEARCHDEPTH = 3;
    private final static int RANDOMSEARCHTRIES = 10;

    private int bestValue = 0;
    private int visitedStates; // measure for states checked

    /**
     * boolean that indicates that the GUI asked the player to stop thinking.
     */
    private boolean stopped;

    public RandomizedAlphaBeta() {
        super("Layers.png");
    }

    @Override
    public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        int depth = 0;
        visitedStates = 0;
        DraughtsNode node = new DraughtsNode(s);    // the root of the search tree

        try {
            while (!stopped && depth < MAXSEARCHDEPTH) {
                // implements iterative deepening up till MAXSEARCHDEPTH
                depth++;

                // compute bestMove and bestValue in a call to alphabeta
                bestValue = alphaBeta(node, MIN_VALUE, MAX_VALUE, depth);

                // store the bestMove found uptill now
                // NB this is not done in case of an AIStoppedException in alphaBeat()
                bestMove = node.getBestMove();
            }
        } catch (AIStoppedException ex) { /* nothing to do */ }

        if (bestMove == null) {
            // When no best move is set, return a random valid move
            System.err.println("no valid move found!");
            return getRandomValidMove(s);
        } else {
            // print the results for debugging reasons
            System.err.format(
                    "%s: depth = %2d, best move = %5s, value = %d\n, discovered = %8d,",
                    this.getClass().getSimpleName(), depth, bestMove, bestValue, visitedStates
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
     * @param depth maximum recursion depth from current state
     * @return the computed value of this node
     * @throws AIStoppedException
     *
     */
    int alphaBeta(DraughtsNode node, int alpha, int beta, int depth) throws AIStoppedException {
        if (stopped) { // stops the player when timeLimit is reached
            stopped = false;
            throw new AIStoppedException();
        }

        visitedStates++;

        DraughtsState state = node.getState();
        if (state.isEndState()) {
            return evaluate(state);
        }
        
        if (depth < 0) {
            return randomPlay(state);
            //return evaluate(state);  
        }

        if (state.isWhiteToMove()) {
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
        DraughtsState state = node.getState();
        List<Move> possibleMoves = state.getMoves();
        Move bestMove = possibleMoves.get(0);
        int foundBeta;
        for (Move move : possibleMoves) {
            state.doMove(move);
            if (move.isCapture()) {
                foundBeta = alphaBeta(new DraughtsNode(state), alpha, beta, depth);
            } else {
                foundBeta = alphaBeta(new DraughtsNode(state), alpha, beta, depth - 1);
            }    
            state.undoMove(move);
            if (beta > foundBeta) {
                bestMove = move;
                beta = foundBeta;
                if (beta <= alpha) {
                    return alpha;
                }
            }
        }
        node.setBestMove(bestMove);
        return beta;
    }

    int alphaBetaMax(DraughtsNode node, int alpha, int beta, int depth) throws AIStoppedException {
        DraughtsState state = node.getState();
        List<Move> possibleMoves = state.getMoves();
        Move bestMove = possibleMoves.get(0);
        int foundAlpha;
        for (Move move : possibleMoves) {
            state.doMove(move);
            if (move.isCapture()) {
                foundAlpha = alphaBeta(new DraughtsNode(state), alpha, beta, depth);
            } else {
                foundAlpha = alphaBeta(new DraughtsNode(state), alpha, beta, depth - 1);
            }
            state.undoMove(move);
            if (alpha < foundAlpha) {
                bestMove = move;
                alpha = foundAlpha;
                if (alpha >= beta) {
                    return beta;
                }
            }
        }
        node.setBestMove(bestMove);
        return alpha;
    }

    /**
     * A method that evaluates the given state.
     */
    int evaluate(DraughtsState state) {
        int[] pieces = state.getPieces();
        int value = 0;
        // uses very simplistic evaluation by piece count.
        for (int piece : pieces) {
            if (piece == WHITEPIECE) {
                value += 100;
            } else if (piece == BLACKPIECE) {
                value -= 100;
            } else if (piece == WHITEKING) {
                value += 500;
            } else if (piece == BLACKKING) {
                value -= 500;
            }
        }
        return value;
    }

    private int randomPlay(DraughtsState rootState) throws AIStoppedException {
        if (stopped) { // stops the player when timeLimit is reached
            stopped = false;
            throw new AIStoppedException();
        }
        int value = 0;
        DraughtsState state;    
        for (int tryNum = 0; tryNum < RANDOMSEARCHTRIES; tryNum++) {
            state = rootState.clone();
            for (int depNum = 0; depNum < RANDOMSEARCHDEPTH; depNum++) {
                List<Move> moves = state.getMoves();
                if (moves.isEmpty()) {
                    break;
                } else {
                    Collections.shuffle(moves);
                    state.doMove(moves.get(0));
                }
            }
            value += evaluate(state);
        }    
        return value / RANDOMSEARCHTRIES;
    }
    
}
