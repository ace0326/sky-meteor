/*
 * The MIT License (MIT)
 * Copyright © 2019-2020 <sky>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sky.meteor.common.spi;

import com.sky.meteor.common.enums.SideEnum;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author
 */
public class SpiLoader {

    /**
     * 加载第一个
     *
     * @param clazz
     * @param <S>
     * @return
     */
    public static <S> S loadFirst(final Class<S> clazz) {
        final ServiceLoader<S> loader = loadAll(clazz);
        final Iterator<S> iterator = loader.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException(String.format(
                    "No implementation defined in /META-INF/services/%s, please check whether the file exists and has the right implementation class!",
                    clazz.getName()));
        }
        return iterator.next();
    }

    /**
     * 加载全部
     *
     * @param clazz
     * @param <S>
     * @return
     */
    public static <S> ServiceLoader<S> loadAll(final Class<S> clazz) {
        return ServiceLoader.load(clazz);
    }

    /**
     * 获取根据优先级排序后的第一个
     *
     * @param clazz
     * @param <S>
     * @return
     */
    public static <S> S loadFirstPriority(final Class<S> clazz) {
        return loadAllPriority(clazz).get(0);
    }

    /**
     * 根据优先级排序后的全部
     *
     * @param clazz
     * @param <S>
     * @return
     */
    public static <S> List<S> loadAllPriority(final Class<S> clazz) {
        final Iterator<S> iterator = loadAll(clazz).iterator();
        List<S> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list, (first, second) -> {
            SpiMetadata firstMetadata = first.getClass().getAnnotation(SpiMetadata.class);
            SpiMetadata secondMetadata = second.getClass().getAnnotation(SpiMetadata.class);
            return firstMetadata.priority() - secondMetadata.priority();
        });
        return list;
    }

    /**
     * @param clazz
     * @param <S>
     * @return
     */
    public static <S> List<S> loadAllPriorityAndSide(final Class<S> clazz, SideEnum side) {
        List<S> list = loadAllPriority(clazz);
        List<S> newList = list.stream().filter(f -> {
            SideEnum sideEnum = f.getClass().getAnnotation(SpiMetadata.class).side();
            return sideEnum.equals(SideEnum.ALL) || sideEnum.equals(side);
        }).collect(Collectors.toList());
        return newList;
    }

    /**
     * 获取对应名称的
     *
     * @param clazz
     * @param name
     * @param <S>
     * @return
     */
    public static <S> S loadByName(final Class<S> clazz, final String name) {
        final Iterator<S> iterator = loadAll(clazz).iterator();
        while (iterator.hasNext()) {
            S next = iterator.next();
            SpiMetadata metadata = next.getClass().getAnnotation(SpiMetadata.class);
            if (metadata.name().equals(name)) {
                return next;
            }
        }
        return null;
    }
}
