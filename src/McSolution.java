import java.util.*;

/**
 * @author Quinten-Lin
 * @date 2020/11/03
 * @desc 基于启发式算法解决M-C问题，启发式函数为 f = m+c-(deltaM+deltaC)*deltaB
 */
public class McSolution {
    /**
     * 两岸的传教士总人数
     */
    private static int M = 3;

    /**
     * 两岸的野人总人数
     */
    private static int C = 3;

    /**
     * 船的总载量
     */
    private static int K = 2;

    /**
     * 船的初始靠岸状态，1为左岸，0为右岸
     */
    private static int B = 1;

    /**
     * 用于存储各种渡河状态的记忆
     * 1表示已经进入close_set, 0表示没有访问过或者状态安全但未进入close_set，-1表示该状态已访问过且不安全
     * 默认值为0
     */
    private static int[][][] set;

    /**
     * 用于记录有效渡河状态（安全状态）空间的最短路径映射
     * String的形式为 m,c,b
     * StateArray为当前状态（m,c,b）的State，通过parent可以回溯到父节点
     */
    public static Map<String, StateArray> pathMap = new HashMap<String, StateArray>();

    public static void main(String[] args) {
        set = new int[M + 1][C + 1][2];
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
     * 用于当前计算代价，即启发式函数的值
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
     * @param m 左岸传教士人数
     * @param c 左岸野人的人数
     * @param b 船的靠岸状态
     * @return
     */
    public static boolean checkSafe(int m, int c, int b) {
        //检查历史标，-1表示曾经遍历过且不安全
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
     * @param m 左岸传教士人数
     * @param c 左岸野人的人数
     * @param b 船的靠岸状态
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
     *
     * @param m 左岸传教士人数
     * @param c 左岸野人的人数
     * @param b 船的靠岸状态
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
                // 从左岸驶向右岸
                b = 0;
                // K为船的总载量，k为本轮渡船的人数
                for (int k = K; k > 0; k--) {
                    for (int i = k, j = 0; i >= 0; i--, j++) {
                        if (m - i < 0 || c - j < 0) {
                            continue;
                        }
                        if (checkSafe(m - i, c - j, b) && !checkIsClosed(m - i, c - j, b)) {
                            String pathKey = (m - i) + "," + (c - j) + "," + b;
                            StateArray sonState;
                            // 子节点先前是否曾经被遍历且已经存在一个深度更浅的访问路径
                            if (pathMap.containsKey(pathKey)) {
                                sonState = pathMap.get(pathKey);
                            } else {
                                sonState = new StateArray(m - i, c - j, b);
                                sonState.setParent(lastState);
                                pathMap.put(pathKey, sonState);
                            }
                            optionalStates.add(sonState);
                        }
                    }
                }
            } else {
                // 从右岸驶向左岸
                b = 1;
                for (int k = 1; k <= K; k++) {
                    for (int i = k, j = 0; i >= 0; i--, j++) {
                        if (m + i > M || c + j > C) {
                            continue;
                        }
                        if (checkSafe(m + i, c + j, b) && !checkIsClosed(m + i, c + j, b)) {
                            String pathKey = (m + i) + "," + (c + j) + "," + b;
                            StateArray sonState;
                            // 子节点先前是否曾经被遍历且已经存在一个深度更浅的访问路径
                            if (pathMap.containsKey(pathKey)) {
                                sonState = pathMap.get(pathKey);
                            } else {
                                sonState = new StateArray(m + i, c + j, b);
                                sonState.setParent(lastState);
                                pathMap.put(pathKey, sonState);
                            }
                            optionalStates.add(sonState);
                        }
                    }
                }
            }
            // 如果该分支没有可行子分支，禁用这个分支并开始回溯，否则继续向下遍历
            if (optionalStates.isEmpty()) {
                if (lastState.getParent() != null) {
                    lastState = lastState.getParent();
                } else {
                    return null;
                }
            } else {
                // 从所有可选状态节点中获取一个代价最小的节点
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
