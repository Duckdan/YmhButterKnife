package com.piaopiao.ymhbutterknife;

/**
 * 用来给用户绑定activity使用
 */
public interface IBinder<T> {
    void bind(T target);
}
