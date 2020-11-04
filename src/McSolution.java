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
     * 用于存储各种状态的记忆
     * 1表示已经进入close_set, 0表示没有访问过，-1表示该状态已访问过且不安全
     * 默认值为0
     */
    private static int[][][] set = new int[4][4][2];

    /**
     * 用于记录有效状态空间的父节点映射
     */
    public static Map<String,StateArray> parentMap = new HashMap<String,StateArray>();

    public static void main(String[] args) {
        beginBoating();
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

    public static boolean checkIsClosed(int m,int c,int b) {
        if (set[m][c][b] == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 模拟左右往返划船
     */
    public static void beginBoating() {
        int m = M;
        int c = C;
        int b = 1;
        StateArray lastState = new StateArray(m, c, b);
        lastState.setParent(null);
        while (!checkFindSolution(m, c, b)) {
            List<StateArray> optionalStates = new ArrayList<>();
            if (b == 1) {
                b = 0;
                for (int i = K, j = 0; i >= 0; i--, j++) {
                    if (checkSafe(m - i, c - j, b) && !checkIsClosed(m - i, c - j, b)) {
                        String parentKey = (m-i) +","+(c-j)+","+b;
                        StateArray sonState = new StateArray(m - i, c - j, b);

                        if (parentMap.containsKey(parentKey)){
                            sonState.setParent(parentMap.get(parentKey));
                        }else{
                            parentMap.put(parentKey, lastState);
                            sonState.setParent(lastState);
                        }
                        optionalStates.add(sonState);
                    }
                }
            } else {
                b = 1;
                for (int i = K, j = 0; i >= 0; i--, j++) {
                    if (checkSafe(m+i,c+j,b) && !checkIsClosed(m+i,c+j,b)) {
                        String parentKey = (m+i) +","+(c+j)+","+b;
                        StateArray sonState = new StateArray(m+i, c+j, b);
                        if (parentMap.containsKey(parentKey)){
                            sonState.setParent(parentMap.get(parentKey));
                        }else{
                            parentMap.put(parentKey, lastState);
                            sonState.setParent(lastState);
                        }
                        optionalStates.add(sonState);
                    }
                }
            }
            if (optionalStates.isEmpty()) {
                throw new RuntimeException("该问题无解！");
            }
            lastState = getBestState(optionalStates);
            m = lastState.getM();
            c = lastState.getC();
            b = lastState.getB();
        }
        printSolution(lastState);
    }

    public static void  printSolution(StateArray lastState) {
        if (lastState.getParent() != null) {
            printSolution(lastState.getParent());
        }
        System.out.println("(" + lastState.getM() + "," + lastState.getC() + "," + lastState.getB() + ")");
    }


}
