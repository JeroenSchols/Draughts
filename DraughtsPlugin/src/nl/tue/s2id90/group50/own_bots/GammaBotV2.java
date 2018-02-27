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
public class GammaBotV2 extends DraughtsPlayer implements DamConstants {

    private final static int MAXSEARCHDEPTH = 200;
    private final static int[][] placeBenefitWhite = 
    {{00, 15, 00, 20, 00, 30, 00, 15, 00, 10},
     {50, 00, 55, 00, 70, 00, 60, 00, 55, 00},
     {00, 15, 00, 20, 00, 30, 00, 15, 00, 10},
     {10, 00, 15, 00, 30, 00, 20, 00, 15, 00},
     {00, 85, 00, 90, 00,100, 00, 85, 00, 10},
     {70, 00, 75, 00, 90, 00, 80, 00, 75, 00},
     {00, 65, 00, 70, 00, 80, 00, 65, 00, 60},
     {30, 00, 35, 00, 50, 00, 40, 00, 35, 00},
     {00, 25, 00, 40, 00, 50, 00, 35, 00, 30},
     {60, 00, 75, 00, 80, 00, 70, 00, 65, 00}};
    private final static int[][] placeBenefitBlack = 
    {{00, 65, 00, 70, 00, 80, 00, 65, 00, 60},
     {20, 00, 25, 00, 40, 00, 30, 00, 25, 00},
     {00, 35, 00, 40, 00, 50, 00, 35, 00, 30},
     {60, 00, 65, 00, 80, 00, 70, 00, 65, 00},
     {00, 75, 00, 80, 00, 90, 00, 75, 00, 70},
     {80, 00, 85, 00,100, 00, 90, 00, 85, 00},
     {00, 15, 00, 20, 00, 30, 00, 15, 00, 10},
     {10, 00, 15, 00, 30, 00, 20, 00, 15, 00},
     {00, 55, 00, 60, 00, 70, 00, 55, 00, 50},
     {10, 00, 15, 00, 30, 00, 20, 00, 15, 00}};

    private int bestValue = 0;

    /**
     * boolean that indicates that the GUI asked the player to stop thinking.
     */
    private boolean stopped;

    public GammaBotV2() {
        super("Gamma.jpg");
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
            if (state.isWhiteToMove()) {
                return Integer.MIN_VALUE;
            } else {
                return Integer.MAX_VALUE;
            }
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
            if (state.isWhiteToMove()) {
                return Integer.MIN_VALUE;
            } else {
                return Integer.MAX_VALUE;
            }
        }
        List<Move> possibleMoves = state.getMoves();
        Move bestMove = possibleMoves.get(0);
       /* if (bestMove.isCapture()) {
            depth++;
        } else*/ if (depth < 0) {
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
        
        final int pieceValue = 10000;
        final int kingValue = 30000;
        final int countBenefit = 750;
        final int sideDominationValue = 1000;
        final int middleDominationValue = 2000;
        final int vFormationValue = 25;
        
        int value = 0;
        int piece;
                
        int whiteDistributionLeft = 0;
        int whiteCountLeft = 0;
        int whiteDistributionMiddle = 0;
        int whiteCountMiddle = 0;
        int whiteDistributionRight = 0;
        int whiteCountRight = 0;
        int blackDistributionLeft = 0;
        int blackCountLeft = 0;
        int blackDistributionMiddle = 0;
        int blackCountMiddle = 0;
        int blackDistributionRight = 0;
        int blackCountRight = 0;
        
        int[][] board = new int[10][10];
        
        //Goes over the board, only checking black squares.
        for (int row = 0; row < 10; row++) {
            for (int col = ((row + 1) % 2); col < 10; col += 2){
                piece = state.getPiece(row, col);
                switch (piece) {
                    case WHITEPIECE:
                        if (col <= 2) {
                            whiteDistributionLeft += placeBenefitWhite[row][col];
                            whiteCountLeft++;
                        } else if (col >= 7){
                            whiteDistributionRight += placeBenefitWhite[row][col];
                            whiteCountRight++;
                        } else {
                            whiteDistributionMiddle += placeBenefitWhite[row][col];
                            whiteCountMiddle++;
                        }
                        board[row][col] = 1;
                        value += pieceValue + placeBenefitWhite[row][col];
                        break;
                    case BLACKPIECE:
                        if (col <= 2) {
                            blackDistributionLeft += placeBenefitBlack[row][col];
                            blackCountLeft++;
                        } else if (col >= 7) {
                            blackDistributionRight += placeBenefitBlack[row][col];
                            blackCountRight++;
                        } else {
                            blackDistributionMiddle += placeBenefitBlack[row][col];
                            whiteCountMiddle++;
                        }
                        board[row][col] = -1;
                        value -= pieceValue + placeBenefitBlack[row][col];
                        break;
                    case WHITEKING:
                        board[row][col] = 1;
                        value += kingValue;
                        break;
                    case BLACKKING:
                        board[row][col] = -1;
                        value -= kingValue;    
                        break;
                }
            }    
        }
        int sumWhiteThree = Math.floorDiv(whiteCountLeft + whiteCountMiddle + whiteCountRight, 3);
        int sumBlackThree = Math.floorDiv(blackCountLeft + blackCountMiddle + blackCountRight, 3);
        
        if (sumWhiteThree - 1 < whiteCountLeft || whiteCountLeft < sumWhiteThree + 2) {
            value += countBenefit;
        }
        if (sumWhiteThree - 1 < whiteCountMiddle || whiteCountMiddle < sumWhiteThree + 2) {
            value += countBenefit;
        }
        if (sumWhiteThree - 1 < whiteCountRight || whiteCountRight < sumWhiteThree + 2) {
            value += countBenefit;
        }
        if (sumBlackThree - 1 < blackCountLeft || blackCountLeft < sumBlackThree + 2) {
            value -= countBenefit;
        }
        if (sumBlackThree - 1 < blackCountMiddle || blackCountMiddle < sumBlackThree + 2) {
            value -= countBenefit;
        }
        if (sumBlackThree - 1 < blackCountRight || blackCountRight < sumBlackThree + 2) {
            value -= countBenefit;
        }

        
        if (whiteDistributionLeft > blackDistributionLeft + 100) {
            value += sideDominationValue;
        }
        if (blackDistributionLeft > whiteDistributionLeft + 100) {
            value -= sideDominationValue;
        }
        if (whiteDistributionMiddle > blackDistributionMiddle + 150) {
            value += middleDominationValue;
        }
        if (blackDistributionMiddle > whiteDistributionMiddle + 150) {
            value -= middleDominationValue;
        }        
        if (whiteDistributionRight > blackDistributionRight + 100) {
            value += sideDominationValue;
        }
        if (blackDistributionRight > whiteDistributionRight + 100) {
            value -= sideDominationValue;
        }
  
        for (int row = 0; row < 10; row++) {
            for (int col = ((row + 1) % 2); col < 10; col += 2){
                if (board[row][col] == -1) {
                    if (row <= 7) {
                        if (col >= 2) {
                            if (board[row + 1][col - 1] == 1 && board[row + 2][col - 2] == 1) {
                                value += (9 - row) * vFormationValue;
                            }
                        } else if (col != 0) {
                            if (board[row + 1][col - 1] == 1) {
                                value += (9 - row) * vFormationValue;
                            }
                        }
                        if (col <= 7) {
                            if (board[row + 1][col + 1] == 1 && board[row + 2][col + 2] == 1) {
                                value += (9 - row) * vFormationValue;
                            }                           
                        } else if (col != 9) {
                            if (board[row + 1][col + 1] == 1) {
                                value += (9 - row) * vFormationValue;
                            }
                        }
                    } else if (row != 9) {
                        if (col != 0) {
                            if (board[row + 1][col - 1] == 1) {
                                value += (9 - row) * vFormationValue;
                            }
                        }
                        if (col != 9) {
                            if (board[row + 1][col + 1] == 1) {
                                value += (9 - row) * vFormationValue;
                            }
                        }
                    }
                } else if (board[row][col] == -1) {
                    if (row >= 2) {
                        if (col >= 2) {
                            if (board[row - 1][col - 1] == -1 && board[row - 2][col - 2] == -1) {
                                value -= row * vFormationValue;
                            }
                        } else if (col != 0) {
                            if (board[row - 1][col - 1] == -1) {
                                value -= row * vFormationValue;
                            }
                        }
                        if (col <= 7) {
                            if (board[row - 1][col + 1] == -1 && board[row - 2][col + 2] == -1) {
                                value -= row * vFormationValue;
                            }                           
                        } else if (col != 9) {
                            if (board[row - 1][col + 1] == -1) {
                                value -= row * vFormationValue;
                            }
                        }
                    } else if (row != 0) {
                        if (col != 0) {
                            if (board[row - 1][col - 1] == -1) {
                                value -= row * vFormationValue;
                            }
                        }
                        if (col != 9) {
                            if (board[row - 1][col + 1] == -1) {
                                value -= row * vFormationValue;
                            }
                        }
                    } 
                }
            }
        }     
        
        return value;
    }
}
