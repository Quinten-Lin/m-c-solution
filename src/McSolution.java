import java.util.*;

/**
 * @author Quinten-Lin
 * @date 2020/11/03
 * @desc 基于启发式算法解决M-C问题
 */
public class McSolution {
    /**
     * 两岸的传教士总人数
     */
    private static int M = 7;

    /**
     * 两岸的野人总人数
     */
    private static int C = 7;

    /**
     * 船的总载量
     */
    private static int K = 3;

    /**
     * 船的初始状态
     */
    private static int B = 1;

    /**
     * 用于存储各种状态的记忆
     * 1表示已经进入close_set, 0表示没有访问过，-1表示该状态已访问过且不安全
     * 默认值为0
     */
    private static int[][][] set;

    /**
     * 用于记录有效状态空间的父节点映射
     */
    public static Map<String, StateArray> parentMap = new HashMap<String, StateArray>();

    public static void main(String[] args) {
        set = new int[M+1][C+1][2];
        StateArray solutionState = beginBoating();
        if (solutionState != null) {
            System.out.println("找到M-C问题(" + M + "," + C + "," + B + ")的解:");
            printSolution(solutionState);
        } else {
            System.err.println("M-C问题(" + M + "," + C + "," + B + ")无解!");
        }
    }

    /**
     * 判断当前状态是否超载
     *
     * @param currentState
     * @return
     */
    public static boolean checkOverload(StateArray currentState) {
        StateArray parentState = currentState.getParent();
        int deltaM = Math.abs(parentState.getM() - currentState.getM());
        int deltaC = Math.abs(parentState.getC() - currentState.getC());
        if (deltaM + deltaC <= K) {
            return true;
        }
        return false;
    }

    /**
     * 用于当前计算代价
     *
     * @param state
     * @return
     */
    public static int getCost(StateArray state) {
        StateArray parentState = state.getParent();
        int deltaM = Math.abs(parentState.getM() - state.getM());
        int deltaC = Math.abs(parentState.getC() - state.getC());
        int deltaB = parentState.getB() - state.getB();
        int result = state.getM() + state.getC() - (deltaC + deltaM) * deltaB;
        return result;
    }

    /**
     * 判断当前状态是否安全
     *
     * @param
     * @return
     */
    public static boolean checkSafe(int m, int c, int b) {
        //检查历史标记
        if (set[m][c][b] == -1) {
            return false;
        }
        //左岸的传教士是否为0或者人数大于野人
        if (m == 0 || m >= c) {
            //右岸的传教士是否为0或者人数大于野人
            if (M - m == 0 || (M - m) >= (C - c)) {
                return true;
            }
        }
        set[m][c][b] = -1;
        return false;
    }

    /**
     * 在可选的状态中选择代价最小的状态，加入close_set
     *
     * @param optionalStates
     * @return
     */
    public static StateArray getBestState(List<StateArray> optionalStates) {
        Iterator<StateArray> iterator = optionalStates.iterator();
        StateArray bestState = iterator.next();
        int minCost = getCost(bestState);
        while (iterator.hasNext()) {
            StateArray state = iterator.next();
            if (getCost(state) < minCost) {
                bestState = state;
            }
        }
        set[bestState.getM()][bestState.getC()][bestState.getB()] = 1;
        return bestState;
    }

    /**
     * 检查是否到达找到解
     *
     * @param m
     * @param c
     * @param b
     * @return
     */
    public static boolean checkFindSolution(int m, int c, int b) {
        if (m == 0 && c == 0 && b == 0) {
            return true;
        }
        return false;
    }

    /**
     * 检查状态是否已经添加到close_set
     * @param m
     * @param c
     * @param b
     * @return
     */
    public static boolean checkIsClosed(int m, int c, int b) {
        if (set[m][c][b] == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 模拟左右往返划船
     */
    public static StateArray beginBoating() {
        int m = M;
        int c = C;
        int b = B;
        StateArray lastState = new StateArray(m, c, b);
        lastState.setParent(null);
        set[m][c][b] = 1;
        while (!checkFindSolution(m, c, b)) {
            List<StateArray> optionalStates = new ArrayList<>();
            if (b == 1) {
                b = 0;
                for (int k = K; k > 0; k--) {
                    for (int i = k, j = 0; i >= 0; i--, j++) {
                        if (m - i < 0 || c - j < 0) {
                            continue;
                        }
                        if (checkSafe(m - i, c - j, b) && !checkIsClosed(m - i, c - j, b)) {
                            String parentKey = (m - i) + "," + (c - j) + "," + b;
                            StateArray sonState = new StateArray(m - i, c - j, b);
                            if (parentMap.containsKey(parentKey)) {
                                sonState.setParent(parentMap.get(parentKey));
                            } else {
                                parentMap.put(parentKey, lastState);
                                sonState.setParent(lastState);
                            }
                            optionalStates.add(sonState);
                        }
                    }
                }
            } else {
                b = 1;
                for (int k = 1; k <= K; k++) {
                    for (int i = k, j = 0; i >= 0; i--, j++) {
                        if (m + i > M || c + j > C) {
                            continue;
                        }
                        if (checkSafe(m + i, c + j, b) && !checkIsClosed(m + i, c + j, b)) {
                            String parentKey = (m + i) + "," + (c + j) + "," + b;
                            StateArray sonState = new StateArray(m + i, c + j, b);
                            if (parentMap.containsKey(parentKey)) {
                                sonState.setParent(parentMap.get(parentKey));
                            } else {
                                parentMap.put(parentKey, lastState);
                                sonState.setParent(lastState);
                            }
                            optionalStates.add(sonState);
                        }
                    }
                }
            }
            //如果该分支没有可行子分支，禁用这个分支并开始回溯，否则继续向下遍历
            if (optionalStates.isEmpty()) {
                set[lastState.getM()][lastState.getC()][lastState.getB()] = -1;
                if (lastState.getParent() != null) {
                    lastState = lastState.getParent();
                } else {
                    return null;
                }
            } else {
                lastState = getBestState(optionalStates);
            }
            m = lastState.getM();
            c = lastState.getC();
            b = lastState.getB();
        }
        return lastState;
    }

    /**
     * 输出M-C问题的解
     *
     * @param lastState
     */
    public static void printSolution(StateArray lastState) {
        if (lastState.getParent() != null) {
            printSolution(lastState.getParent());
        }
        System.out.println("(" + lastState.getM() + "," + lastState.getC() + "," + lastState.getB() + ")");
    }
}
