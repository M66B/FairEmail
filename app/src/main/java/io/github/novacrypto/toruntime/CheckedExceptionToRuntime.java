/*
 *  ToRuntime library, Java promotions of checked exceptions to runtime exceptions
 *  Copyright (C) 2017-2022 Alan Evans, NovaCrypto
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Original source: https://github.com/NovaCrypto/ToRuntime
 *  You can contact the authors via github issues.
 */

package io.github.novacrypto.toruntime;

/**
 * Promotes any exceptions thrown to {@link RuntimeException}
 */
public final class CheckedExceptionToRuntime {

    public interface Func<T> {
        T run() throws Exception;
    }

    public interface Action {
        void run() throws Exception;
    }

    /**
     * Promotes any exceptions thrown to {@link RuntimeException}
     *
     * @param function Function to run
     * @param <T>      Return type
     * @return returns the result of the function
     */
    public static <T> T toRuntime(final Func<T> function) {
        try {
            return function.run();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Promotes any exceptions thrown to {@link RuntimeException}
     *
     * @param function Function to run
     */
    public static void toRuntime(final Action function) {
        try {
            function.run();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}