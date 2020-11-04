/**
 * @author Quinten-Lin
 * @date 2020/11/03
 * @desc 左岸的状态数组
 */
public class StateArray {
    /**
     * 当前左岸的传教士人数
     */
    private int m;

    /**
     * 当前左岸的野人人数
     */
    private int c;

    /**
     * 当前船的靠岸状态，1为左岸，0为右岸
     */
    private int b;

    /**
     * 状态空间的父节点，方便回溯
     */
    private StateArray parent;

    public StateArray(int m, int c, int b) {
        this.m = m;
        this.c = c;
        this.b = b;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public StateArray getParent() {
        return parent;
    }

    public void setParent(StateArray parent) {
        this.parent = parent;
    }
}
