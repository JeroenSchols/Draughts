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
 Implementation of the basic required player.

 @author Jeroen, Andreas
 */
public class Fluffles extends DraughtsPlayer implements DamConstants {

    private final static int MAXSEARCHDEPTH = 200;

    // indicates the worth of occupying a space
    private final static int[][] placeBenefitWhite
            = {{00, 15, 00, 20, 00, 30, 00, 15, 00, 10},
            {50, 00, 55, 00, 70, 00, 60, 00, 55, 00},
            {00, 15, 00, 20, 00, 30, 00, 15, 00, 10},
            {10, 00, 15, 00, 30, 00, 20, 00, 15, 00},
            {00, 85, 00, 90, 00, 100, 00, 85, 00, 10},
            {70, 00, 75, 00, 90, 00, 80, 00, 75, 00},
            {00, 65, 00, 70, 00, 80, 00, 65, 00, 60},
            {30, 00, 35, 00, 50, 00, 40, 00, 35, 00},
            {00, 25, 00, 40, 00, 50, 00, 35, 00, 30},
            {50, 00, 65, 00, 70, 00, 60, 00, 55, 00}};
    private final static int[][] placeBenefitBlack
            = {{00, 55, 00, 60, 00, 70, 00, 55, 00, 50},
            {20, 00, 25, 00, 40, 00, 30, 00, 25, 00},
            {00, 35, 00, 40, 00, 50, 00, 35, 00, 30},
            {60, 00, 65, 00, 80, 00, 70, 00, 65, 00},
            {00, 75, 00, 80, 00, 90, 00, 75, 00, 70},
            {80, 00, 85, 00, 100, 00, 90, 00, 85, 00},
            {00, 15, 00, 20, 00, 30, 00, 15, 00, 10},
            {10, 00, 15, 00, 30, 00, 20, 00, 15, 00},
            {00, 55, 00, 60, 00, 70, 00, 55, 00, 50},
            {10, 00, 15, 00, 30, 00, 20, 00, 15, 00}};

    private int bestValue = 0; // best value found thus far
    private Move bestFirstMove; // best first move found thus far

    /**
     boolean that indicates that the GUI asked the player to stop thinking.
     */
    private boolean stopped;

    public Fluffles() {
        super("Gamma.jpg");
    }

    @Override
    public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        int depth = 0;
        DraughtsNode node = new DraughtsNode(s); // the root of the search tree
        bestFirstMove = s.getMoves().get(0); // initially set any node to first consider
        try {
            while (!stopped && depth < MAXSEARCHDEPTH) {
                // implements iterative deepening up till MAXSEARCHDEPTH
                depth++;

                // compute bestMove, bestFirstMove and bestValue in a call to alphabeta
                bestValue = alphaBeta(node, MIN_VALUE, MAX_VALUE, depth);
                bestFirstMove = node.getBestMove();

                // store the bestMove found uptill now
                // NB this is not done in case of an AIStoppedException in alphaBeat()
                bestMove = node.getBestMove();
            }
        } catch (Exception ex) { // use any exception, so bot does not crash after unintended exception
            /* nothing to do */
        }

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
     This method's return value is displayed in the AICompetition GUI.

     @return the value for the draughts state s as it is computed in a call to
     getMove(s).
     */
    @Override
    public Integer getValue() {
        return bestValue;
    }

    /**
     Tries to make alphabeta search stop. Search should be implemented such
     that it throws an AIStoppedException when boolean stopped is set to true;
     */
    @Override
    public void stop() {
        stopped = true;
    }

    /**
     returns random valid move in state s, or null if no moves exist.
     */
    Move getRandomValidMove(DraughtsState s) {
        List<Move> moves = s.getMoves();
        Collections.shuffle(moves);
        return moves.isEmpty() ? null : moves.get(0);
    }

    /**
     Implementation of alphabeta that automatically chooses the white player
     as maximizing player and the black player as minimizing player.

     @param node contains DraughtsState and has field to which the best move
     can be assigned.
     @param alpha
     @param beta
     @param depth maximum recursion depth from current state
     @return the computed value of this node
     @throws AIStoppedException

     */
    int alphaBeta(DraughtsNode node, int alpha, int beta, int depth) throws AIStoppedException {
        DraughtsState state = node.getState();
        if (state.isWhiteToMove()) { // depends on which colour this bot is
            return alphaBetaMax(node, alpha, beta, depth, true);
        } else {
            return alphaBetaMin(node, alpha, beta, depth, true);
        }
    }

    /**
     Does an alphabeta computation with the given alpha and beta where the
     player that is to move in node is the minimizing player.

     <p>
     Typical pieces of code used in this method are:
     <ul> <li><code>DraughtsState state = node.getState()</code>.</li>
     <li><code> state.doMove(move); .... ; state.undoMove(move);</code></li>
     <li><code>node.setBestMove(bestMove);</code></li>
     <li><code>if(stopped) { stopped=false; throw new AIStoppedException(); }</code></li>
     </ul>
     </p>

     @param node contains DraughtsState and has field to which the best move
     can be assigned.
     @param alpha
     @param beta
     @param depth maximum recursion Depth
     @param firstMove indicates whether we are at the root node
     @return the compute value of this node
     @throws AIStoppedException thrown whenever the boolean stopped has been
     set to true.
     */
    int alphaBetaMin(DraughtsNode node, int alpha, int beta, int depth, boolean firstMove) throws AIStoppedException {
        if (stopped) { // stops the player when timeLimit is reached
            stopped = false;
            throw new AIStoppedException();
        }

        DraughtsState state = node.getState();
        if (state.isEndState()) { // when we are in an end state we can not move deeper and immediatly evaluate
            return evaluate(state);
        }

        List<Move> possibleMoves = state.getMoves();

        Move bestMove;
        if (firstMove) { // when we are at the root node set the bestMove to be the bestMove from the previous iteration
            bestMove = bestFirstMove;
        } else {
            bestMove = possibleMoves.get(0);
            possibleMoves.remove(0);
        }

        if (bestMove.isCapture()) { // we do not count capturing moves to our depth, as these are considered more important, and allow us to always end in a quiet state
            depth++;
        } else if (depth < 0) {
            return evaluate(state);
        }

        // checks the bestMove as first move to hope for better beta values that allows more pruning of search tree
        int foundBeta;
        state.doMove(bestMove);
        foundBeta = alphaBetaMax(new DraughtsNode(state), alpha, beta, depth - 1, false);
        state.undoMove(bestMove);
        if (beta > foundBeta) {
            beta = foundBeta;
            if (beta <= alpha) {
                return alpha;
            }
        }

        // checks all other possible moves in the search tree
        for (Move move : possibleMoves) {
            state.doMove(move);
            foundBeta = alphaBetaMax(new DraughtsNode(state), alpha, beta, depth - 1, false);
            state.undoMove(move);
            if (beta > foundBeta) {
                bestMove = move;
                beta = foundBeta;
                if (beta <= alpha) {
                    return alpha;
                }
            }
        }

        // set the best move and return the found beta
        node.setBestMove(bestMove);
        return beta;
    }

    int alphaBetaMax(DraughtsNode node, int alpha, int beta, int depth, boolean firstMove) throws AIStoppedException {
        if (stopped) { // stops the player when timeLimit is reached
            stopped = false;
            throw new AIStoppedException();
        }

        DraughtsState state = node.getState();
        if (state.isEndState()) { // when we are in an end state we can not move deeper and immediatly evaluate
            return evaluate(state);
        }

        List<Move> possibleMoves = state.getMoves();

        Move bestMove;
        if (firstMove) { // when we are at the root node set the bestMove to be the bestMove from the previous iteration
            bestMove = bestFirstMove;
        } else {
            bestMove = possibleMoves.get(0);
            possibleMoves.remove(0);
        }

        if (bestMove.isCapture()) { // we do not count capturing moves to our depth, as these are considered more important, and allow us to always end in a quiet state
            depth++;
        } else if (depth < 0) {
            return evaluate(state);
        }

        // checks the bestMove as first move to hope for better beta values that allows more pruning of search tree
        int foundAlpha;
        state.doMove(bestMove);
        foundAlpha = alphaBetaMin(new DraughtsNode(state), alpha, beta, depth - 1, false);
        state.undoMove(bestMove);
        if (alpha < foundAlpha) {
            alpha = foundAlpha;
            if (alpha >= beta) {
                return beta;
            }
        }

        // checks all other possible moves in the search tree
        for (Move move : possibleMoves) {
            state.doMove(move);
            foundAlpha = alphaBetaMin(new DraughtsNode(state), alpha, beta, depth - 1, false);
            state.undoMove(move);
            if (alpha < foundAlpha) {
                bestMove = move;
                alpha = foundAlpha;
                if (alpha >= beta) {
                    return beta;
                }
            }
        }

        // set the best move and return the found alpha
        node.setBestMove(bestMove);
        return alpha;
    }

    /**
     A method that evaluates the given state.
     */
    int evaluate(DraughtsState state) {

        // list of parameters used for evaluation. Change these depending on results from test games
        final int pieceValue = 10000;
        final int kingValue = 30000;
        final int evenDistributionValue = 500; // beneficial to have pieces evenly spread over the board
        final int sideDominationValue = 100; // value to receive for dominating a side (this value is large as it depends on how much it dominates)
        final int middleDominationValue = 130; // value to receive for dominating the middle (this value is large as it depends on how much it dominates)
        final int vFormationValue = 20; // value to receive for mainting a safe/protected V shaped position (this value is large as it depends on the position on the board)

        int value = 0;
        int piece;

        // values that indicate how many pieces are on a side of the board
        int whiteCountLeft = 0;
        int whiteCountMiddle = 0;
        int whiteCountRight = 0;
        int blackCountLeft = 0;
        int blackCountMiddle = 0;
        int blackCountRight = 0;

        // saves all pieces received for faster access
        int[][] board = new int[10][10];

        //Goes over the board, only checking black squares.
        for (int row = 0; row < 10; row++) {
            for (int col = ((row + 1) % 2); col < 10; col += 2) {
                piece = state.getPiece(row, col);
                switch (piece) {

                case WHITEPIECE:
                    // counts where it is on the board
                    if (col <= 2) {
                        whiteCountLeft++;
                    } else if (col >= 7) {
                        whiteCountRight++;
                    } else {
                        whiteCountMiddle++;
                    }

                    board[row][col] = 1;
                    value += pieceValue + placeBenefitWhite[row][col] * 10;
                    break;

                case BLACKPIECE:
                    // counts where it is on the board
                    if (col <= 2) {
                        blackCountLeft++;
                    } else if (col >= 7) {
                        blackCountRight++;
                    } else {
                        blackCountMiddle++;
                    }

                    board[row][col] = -1;
                    value -= pieceValue + placeBenefitBlack[row][col] * 10;
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

        // when we are at an end state
        if (state.isEndState()) {
            if (state.isWhiteToMove()) {
                if (whiteCountLeft + whiteCountMiddle + whiteCountRight == 0) {
                    // black won is very low evaluation
                    return Integer.MIN_VALUE / 2;
                } else {
                    // we tied
                    return 0;
                }
            } else {
                if (blackCountLeft + blackCountMiddle + blackCountRight == 0) {
                    // white won is very high evaluation
                    return Integer.MAX_VALUE / 2;
                } else {
                    // we tied
                    return 0;
                }         
            }
        }

        
        
        // gets 1/3 of the number of pieces each player has
        int sumWhiteThree = Math.floorDiv(whiteCountLeft + whiteCountMiddle + whiteCountRight, 3);
        int sumBlackThree = Math.floorDiv(blackCountLeft + blackCountMiddle + blackCountRight, 3);

        // adds value when the board is evenly spreaded
        if (sumWhiteThree - 1 < whiteCountLeft || whiteCountLeft < sumWhiteThree + 2) {
            value += evenDistributionValue;
        }
        if (sumWhiteThree - 1 < whiteCountMiddle || whiteCountMiddle < sumWhiteThree + 2) {
            value += evenDistributionValue;
        }
        if (sumWhiteThree - 1 < whiteCountRight || whiteCountRight < sumWhiteThree + 2) {
            value += evenDistributionValue;
        }

        if (sumBlackThree - 1 < blackCountLeft || blackCountLeft < sumBlackThree + 2) {
            value -= evenDistributionValue;
        }
        if (sumBlackThree - 1 < blackCountMiddle || blackCountMiddle < sumBlackThree + 2) {
            value -= evenDistributionValue;
        }
        if (sumBlackThree - 1 < blackCountRight || blackCountRight < sumBlackThree + 2) {
            value -= evenDistributionValue;
        }

        // when a player dominates a side/middle, generate value depending on how much it is dominated by
        if (whiteCountLeft > blackCountLeft) {
            value += sideDominationValue * Math.pow(whiteCountLeft - blackCountLeft, 2);
        } else if (whiteCountLeft < blackCountLeft) {
            value -= sideDominationValue * Math.pow(whiteCountLeft - blackCountLeft, 2);
        }
        if (whiteCountRight > blackCountRight) {
            value += sideDominationValue * Math.pow(whiteCountRight - blackCountRight, 2);
        } else if (whiteCountRight < blackCountRight) {
            value -= sideDominationValue * Math.pow(whiteCountRight - blackCountRight, 2);
        }
        if (whiteCountMiddle > blackCountMiddle) {
            value += middleDominationValue * Math.pow(whiteCountMiddle - blackCountMiddle, 2);
        } else if (whiteCountMiddle < blackCountMiddle) {
            value -= middleDominationValue * Math.pow(whiteCountMiddle - blackCountMiddle, 2);
        }

        // a mess of code to determine if there are V-Positions. Many if cases as throwing ArrayIndexOutOfBounds Exception are far more expensive
        for (int row = 0; row < 10; row++) {
            for (int col = ((row + 1) % 2); col < 10; col += 2) {
                if (board[row][col] == -1) {
                    if (row <= 7) {
                        if (col >= 3) {
                            if (board[row + 1][col - 1] == 1 && board[row + 2][col - 2] == 1) {
                                value += Math.pow(9 - row, 2) * vFormationValue;
                            }
                        } else if (col == 1) {
                            if (board[row + 1][col - 1] == 1) {
                                value += Math.pow(9 - row, 2) * vFormationValue / 2;
                            }
                        }
                        if (col <= 6) {
                            if (board[row + 1][col + 1] == 1 && board[row + 2][col + 2] == 1) {
                                value += Math.pow(9 - row, 2) * vFormationValue;
                            }
                        } else if (col == 8) {
                            if (board[row + 1][col + 1] == 1) {
                                value += Math.pow(9 - row, 2) * vFormationValue / 2;
                            }
                        }
                    } else if (row == 8) {
                        if (col == 1) {
                            if (board[row + 1][col - 1] == 1) {
                                value += Math.pow(9 - row, 2) * vFormationValue / 2;
                            }
                        }
                        if (col == 8) {
                            if (board[row + 1][col + 1] == 1) {
                                value += Math.pow(9 - row, 2) * vFormationValue / 2;
                            }
                        }
                    }
                } else if (board[row][col] == -1) {
                    if (row >= 2) {
                        if (col >= 3) {
                            if (board[row - 1][col - 1] == -1 && board[row - 2][col - 2] == -1) {
                                value -= Math.pow(row, 2) * vFormationValue;
                            }
                        } else if (col == 1) {
                            if (board[row - 1][col - 1] == -1) {
                                value -= Math.pow(row, 2) * vFormationValue / 2;
                            }
                        }
                        if (col <= 6) {
                            if (board[row - 1][col + 1] == -1 && board[row - 2][col + 2] == -1) {
                                value -= Math.pow(row, 2) * vFormationValue;
                            }
                        } else if (col == 8) {
                            if (board[row - 1][col + 1] == -1) {
                                value -= Math.pow(row, 2) * vFormationValue / 2;
                            }
                        }
                    } else if (row != 0) {
                        if (col == 1) {
                            if (board[row - 1][col - 1] == -1) {
                                value -= Math.pow(row, 2) * vFormationValue / 2;
                            }
                        }
                        if (col == 8) {
                            if (board[row - 1][col + 1] == -1) {
                                value -= Math.pow(row, 2) * vFormationValue / 2;
                            }
                        }
                    }
                }
            }
        }

        // return the value on the board
        return value;
    }
}
