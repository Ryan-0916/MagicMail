package com.magicrealms.magicmail.core.utils;

import java.util.Objects;

/**
 * @author Ryan-0916
 * @Desc 元数组
 * @date 2025-06-04
 */
public record Tuple<F, S, T, F2>(F first, S second, T third, F2 fourth) {

    public static <F, S, T, F2> Tuple<F, S, T, F2> of(F first, S second, T third, F2 fourth) {
        return new Tuple<>(first, second, third, fourth);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Tuple<?, ?, ?, ?> tuple = (Tuple<?, ?, ?, ?>) object;
        return Objects.equals(first, tuple.first)
                && Objects.equals(second, tuple.second)
                && Objects.equals(third, tuple.third)
                && Objects.equals(fourth, tuple.fourth);
    }


    public int hashCode() {
        return Objects.hash(this.first, this.second, this.third, this.fourth);
    }

}
