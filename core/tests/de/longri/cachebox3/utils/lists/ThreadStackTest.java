/*
 * Copyright (C) 2017 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.utils.lists;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 06.01.2017.
 */
class ThreadStackTest {

    static class TestCancelRunnable implements CancelRunable {

        private final String name;

        private boolean cancel = false;

        TestCancelRunnable(String name) {
            this.name = name;
        }

        @Override
        public void cancel() {
            cancel = true;
        }

        @Override
        public void run() {
            System.out.println("Start Runnable " + name);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Finish Runnable " + name);
            System.out.println();
        }
    }

    @Test
    void ConstructorTest() {
        ThreadStack<TestCancelRunnable> runnables = new ThreadStack<TestCancelRunnable>();
        assertThat("Must constructable", runnables != null);
        assertThat("Max item size must be 1", runnables.getMaxItemSize() == 1);

        runnables.dispose();

        ThreadStack<TestCancelRunnable> limetedRunnables = new ThreadStack<TestCancelRunnable>(10);
        assertThat("Must constructable", limetedRunnables != null);
        assertThat("Max item size must be 10", limetedRunnables.getMaxItemSize() == 10);

        limetedRunnables.dispose();
    }

    @Test
    void runTest() {
        ThreadStack<TestCancelRunnable> runnables = new ThreadStack<TestCancelRunnable>();
        TestCancelRunnable runnable = new TestCancelRunnable("SingleRunnable");

        runnables.pushAndStart(runnable);


        sleep(100);

        //wait for ready with work
        while (runnables.isReadyAndEmpty()) {

        }
    }

    @Test
    void runTest2() {
        ThreadStack<TestCancelRunnable> runnables = new ThreadStack<TestCancelRunnable>();
        TestCancelRunnable runnable1 = new TestCancelRunnable("Runnable 1");
        TestCancelRunnable runnable2 = new TestCancelRunnable("Runnable 2");
        TestCancelRunnable runnable3 = new TestCancelRunnable("Runnable 3");


        runnables.pushAndStart(runnable1);
        sleep(5);
        runnables.pushAndStart(runnable2);
        sleep(5);
        runnables.pushAndStart(runnable3);


        sleep(20);

        //wait for ready with work
        while (runnables.isReadyAndEmpty()) {

        }
    }

    private void sleep(int value) {
        try {
            Thread.sleep(value);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}