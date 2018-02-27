package nl.tue.s2id90.group50.own_bots;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import nl.tue.s2id90.group50.AIStoppedException;
import nl.tue.s2id90.group50.DraughtsNode;
import org10x10.dam.game.DamConstants;
import static org10x10.dam.game.DamConstants.BLACKKING;
import static org10x10.dam.game.DamConstants.BLACKPIECE;
import static org10x10.dam.game.DamConstants.WHITEKING;
import static org10x10.dam.game.DamConstants.WHITEPIECE;
import org10x10.dam.game.Move;

/**
 * Implementation of the basic required player.
 *
 * @author Jeroen, Andreas
 */
public class GammaBot extends DraughtsPlayer implements DamConstants {

    private final static int MAXSEARCHDEPTH = 200;
    private int bestValue = 0;

    /**
     * boolean that indicates that the GUI asked the player to stop thinking.
     */
    private boolean stopped;

    public GammaBot() {
        super("Layers.png");
    }

    @Override
    public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        int depth = 0; // set this to a better depth at the end of creation
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
        } catch (AIStoppedException ex) {
            /* nothing to do */ }

        if (bestMove == null) {
            // When no best move is set, return a random valid move
            System.err.println("no valid move found!");
            return getRandomValidMove(s);
        } else {
            // print the results for debugging reasons
            System.err.format(
                    "%s: depth = %2d, best move = %5s, value = %d\n,",
                    this.getClass().getSimpleName(), depth, bestMove, bestValue
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
        DraughtsState state = node.getState();
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
        if (stopped) { // stops the player when timeLimit is reached
            stopped = false;
            throw new AIStoppedException();
        }
        DraughtsState state = node.getState();
        if (state.isEndState()) {
            return evaluate(state);
        }
        List<Move> possibleMoves = state.getMoves();
        Move bestMove = possibleMoves.get(0);
        if (bestMove.isCapture()) {
            depth++;
        } else if (depth < 0) {
            return evaluate(state);
        }
        int foundBeta;
        for (Move move : possibleMoves) {
            state.doMove(move);
            foundBeta = alphaBetaMax(new DraughtsNode(state), alpha, beta, depth - 1);
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
        if (stopped) { // stops the player when timeLimit is reached
            stopped = false;
            throw new AIStoppedException();
        }
        DraughtsState state = node.getState();
        if (state.isEndState()) {
            return evaluate(state);
        }
        List<Move> possibleMoves = state.getMoves();
        Move bestMove = possibleMoves.get(0);
        if (bestMove.isCapture()) {
            depth++;
        } else if (depth < 0) {
            return evaluate(state);
        }
        int foundAlpha;
        for (Move move : possibleMoves) {
            state.doMove(move);
            foundAlpha = alphaBetaMin(new DraughtsNode(state), alpha, beta, depth - 1);
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
        if (true) {
            return evaluate2(state);
        }
        
        int value = 0;
        for (int r = 0; r < 10; r++) {
            int rm2 = r%2;
            for (int c = 1 + rm2; c < 10; c += 2) {
                int piece = state.getPiece(r, c);
                if (piece == WHITEPIECE) {
                    value += 10000;
                } else if (piece == BLACKPIECE) {
                    value -= 10000;
                } else if (piece == WHITEKING) {
                    value += 30000;
                } else if (piece == BLACKKING) {
                    value -= 30000;
                }
            }
        }
        return value;
    }
    
    /**
     * A method that evaluates the given state.
     */
    int evaluate2(DraughtsState state) {
        //sets the value of the piece
        int value_piece = 10000;
        int value_king = value_piece*3;
        
        boolean skip;
        int value = 0;
        int white_tile_correction;
        int colour = 0;
        int piece = 0;
        
        // n_white = number of white pieces
        // the w_distribution or the b_distribution hold the number of white or black pieces at the left of the board [0] centre [1] and right side [2].
        int n_white = 0;
        int[] w_distribution = new int[]{0, 0, 0};
        int[] b_distribution = new int[]{0, 0, 0};
        
        //Goes over the board, only checking black squares.
        for (int row = 0; row < 10; row++) {
            white_tile_correction = ((row + 1) % 2); //skips a square if the row is even.
            for (int col = white_tile_correction; col < 10; col += 2){
                piece = state.getPiece(row, col);
                skip = false;
                //Add piece or king value and count the pieces.
                switch (piece) {
                    case WHITEPIECE:
                        value+= value_piece;
                        colour = 1;
                        n_white++;
                        break;
                    case BLACKPIECE:
                        value-= value_piece;
                        colour = -1;
//                        n_black++;
                        break;
                    case WHITEKING:
                        value += value_king;
                        colour = 1;
                        skip = true;
                        n_white++;
                        break;
                    case BLACKKING:
                        value -= value_king;
                        colour = -1;
                        skip = true;
//                        n_black++;
                        break;
                    default:
                        skip = true;
                        break;
                }
                //If it is not a king, who can move around, and it is not an empty tile
                //  assign value for its positioning on the board
                if (!skip){
                    //Value for how far it is on the board. Now a guassian-like distribution
                    int row_value;
                    if (colour == 1) {
                      row_value = Math.abs(row-9);
                    } else {
                        row_value = row;
                    }
                    value += Math.ceil(Math.abs(Math.abs(row_value-4.5)-4.5))*colour;;
                    
                    //Value for how centered the pieces are on the board
                    value += Math.ceil(Math.abs(Math.abs(col-4.5)-4.5))*colour;
                    //Assign distribution on the 3 sides of the board
                    if (colour == 1){
                        if (col >= 0 && col <= 2){
                            w_distribution[0] ++;
                        }else if (col >=3 && col <= 6) {
                            w_distribution[1] ++;
                        }else {
                            w_distribution[2] ++;
                        }
                    } 
               
                    if (colour == -1){
                        if (col >= 0 && col <= 2){
                            b_distribution[0] ++;
                        }else if (col >=3 && col <= 6) {
                            b_distribution[1] ++;
                        }else {
                            b_distribution[2] ++;
                        }
                    } 
                    
                    //Check if pieces are grouped together (better defense)
                    int neighbourhood_val = check_neighbourhood(row, col, state, colour);
                    int neighbourhood_value = 2;
                    value += neighbourhood_val*colour * neighbourhood_value;
                }
            }
        }
        int dist_val = 0;
        int check = 0;
        //Check for how distributed the white pieces are.
        if (w_distribution[0] == Math.ceil(n_white/3.0) || w_distribution[0] == Math.floor(n_white/3.0)) {
            dist_val+= 10;
            check++;
        }
        if (w_distribution[1] == Math.ceil(n_white/3.0) || w_distribution[1] == Math.floor(n_white/3.0)) {
            dist_val+= 15;
            check++;
        }
        if (w_distribution[2] == Math.ceil(n_white/3.0) || w_distribution[2] == Math.floor(n_white/3.0)) {
            dist_val+= 10;
            check++;
        }
        value+= dist_val;
        //Award  uniform distribution on the board
        if (check == 3) {
            value+=20;
        }
        //Award better distribution than opponent
        value+= (w_distribution[0] > b_distribution[0]) ? 20 : -20;
        value+= (w_distribution[1] > b_distribution[1]) ? 25 : -25;
        value+= (w_distribution[2] > b_distribution[2]) ? 20 : -20;

        return value;
    }
  
    
    int check_neighbourhood(int row, int col, DraughtsState state, int colour){
        int val = 0;
        int piece;
        int tile_correction;
        int row_value = 0;
        if (colour == 1) {
            row_value += Math.abs(row-9);
        }else {
            row_value = row;
        }
        //Check neighbourhood in a collumn like manner
        for (int i = -2; i <= 2; i++){
            tile_correction = ((Math.abs(i) % 2) == 0) ? 1 : 0;
            for (int j = -1 + tile_correction; j <= 1; j+=2){
                if (row + i >= 0 && col+j >= 0 && row + i <= 9 && col+j <= 9 && !(i== 0 & j== 0)){
                    piece = state.getPiece(row+i, col+j);
                    if (piece != 0){
                        if (colour == 1 && (piece == 1 /*| piece == 3*/)){
                            val++;
                        }else if (colour == -1 && (piece == 2 /*|| piece == 4*/)){
                            val++;
                        }
                    }
                }
            }
        }
        //award protecting pieces furter up in the board.
        val *= Math.ceil(Math.exp(row_value-4.5));
        return val;
    }

}
