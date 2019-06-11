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

import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.TestUtils;
import org.junit.jupiter.api.Test;
import travis.EXCLUDE_FROM_TRAVIS;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Longri on 06.01.2017.
 */
class ThreadStackTest {

    static {
        TestUtils.initialGdx();
    }

    AtomicBoolean TestIsRunning = new AtomicBoolean(false);


    class TestCancelRunnable implements CancelRunable {

        private final StringBuilder stringBuilder;

        private final String name;
        private boolean cancel = false;

        TestCancelRunnable(StringBuilder stringBuilder, String name) {
            this.stringBuilder = stringBuilder;
            this.name = name;
        }

        @Override
        public void cancel() {
            cancel = true;
        }

        @Override
        public void run() {
            stringBuilder.append("Start Runnable " + name + "\n");
            int count = 0;
            while (!cancel && count < 20) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count++;
            }

            if (count < 20) {
                stringBuilder.append("Cancel Runnable " + name + "\n");
                stringBuilder.append("\n");
            } else {
                stringBuilder.append("Finish Runnable " + name + "\n");
                stringBuilder.append("\n");
            }

        }
    }

    @Test
    void ConstructorTest() {
        if (EXCLUDE_FROM_TRAVIS.VALUE) return;
        synchronized (TestIsRunning) {
            ThreadStack<TestCancelRunnable> runnables = new ThreadStack<TestCancelRunnable>();
            assertThat("Must constructable", runnables != null);
            assertThat("Max item size must be 1", runnables.getMaxItemSize() == 1);

            runnables.dispose();

            ThreadStack<TestCancelRunnable> limetedRunnables = new ThreadStack<TestCancelRunnable>(10);
            assertThat("Must constructable", limetedRunnables != null);
            assertThat("Max item size must be 10", limetedRunnables.getMaxItemSize() == 10);

            limetedRunnables.dispose();
        }
    }

    @Test
    void runTest() {
        if (EXCLUDE_FROM_TRAVIS.VALUE) return;
        synchronized (TestIsRunning) {
            StringBuilder stringBuilder = new StringBuilder();

            ThreadStack<TestCancelRunnable> runnables = new ThreadStack<TestCancelRunnable>();
            TestCancelRunnable runnable = new TestCancelRunnable(stringBuilder, "SingleRunnable");

            runnables.pushAndStart(runnable);

            sleep(1000);

            //wait for ready with work
            int testCount = 0;
            while (!runnables.isReadyAndEmpty()) {
                sleep(10);
                testCount++;
            }

            System.out.println("TestCount:" + testCount);
            System.out.print(stringBuilder.toString());
            System.out.flush();


            StringBuilder testStringBuilder = new StringBuilder();
            testStringBuilder.append("Start Runnable SingleRunnable\n");
            testStringBuilder.append("Finish Runnable SingleRunnable\n\n");

            assertThat("Threat must start and finish", testStringBuilder.equals(stringBuilder));
        }
    }

    @Test()
    void runTest2() {
        if (EXCLUDE_FROM_TRAVIS.VALUE) return;
        synchronized (TestIsRunning) {
            StringBuilder stringBuilder = new StringBuilder();
            ThreadStack<TestCancelRunnable> runnables = new ThreadStack<TestCancelRunnable>();
            TestCancelRunnable runnable1 = new TestCancelRunnable(stringBuilder, "Runnable 1");
            TestCancelRunnable runnable2 = new TestCancelRunnable(stringBuilder, "Runnable 2");
            TestCancelRunnable runnable3 = new TestCancelRunnable(stringBuilder, "Runnable 3");


            runnables.pushAndStart(runnable1);
            sleep(5);
            runnables.pushAndStart(runnable2);
            sleep(5);
            runnables.pushAndStart(runnable3);


            sleep(1000);

            //wait for ready with work
            int testCount = 0;
            while (!runnables.isReadyAndEmpty()) {
                sleep(10);
                testCount++;
            }

            System.out.println("TestCount:" + testCount);

            System.out.print(stringBuilder.toString());
            System.out.flush();

            StringBuilder testStringBuilder = new StringBuilder();
            testStringBuilder.append("Start Runnable Runnable 1\n");
            testStringBuilder.append("Finish Runnable Runnable 1\n\n");

            testStringBuilder.append("Start Runnable Runnable 3\n");
            testStringBuilder.append("Finish Runnable Runnable 3\n\n");

            assertThat("Runnable1 must start and finish before Runnable3," +
                    " Runnable2 must ignored", testStringBuilder.equals(stringBuilder));
        }
    }

    @Test()
    void runTest3() {
        if (EXCLUDE_FROM_TRAVIS.VALUE) return;
        synchronized (TestIsRunning) {
            StringBuilder stringBuilder = new StringBuilder();
            ThreadStack<TestCancelRunnable> runnables = new ThreadStack<TestCancelRunnable>();
            TestCancelRunnable runnable1 = new TestCancelRunnable(stringBuilder, "Runnable 1");
            TestCancelRunnable runnable2 = new TestCancelRunnable(stringBuilder, "Runnable 2");
            TestCancelRunnable runnable3 = new TestCancelRunnable(stringBuilder, "Runnable 3");
            TestCancelRunnable runnable4 = new TestCancelRunnable(stringBuilder, "Runnable 4");
            TestCancelRunnable runnable5 = new TestCancelRunnable(stringBuilder, "Runnable 5");


            runnables.pushAndStart(runnable1);
            sleep(20);
            runnables.pushAndStart(runnable2);
            sleep(10);
            runnables.pushAndStart(runnable3);
            sleep(100);
            runnables.pushAndStart(runnable4);
            sleep(10);
            runnables.pushAndStart(runnable5);


            sleep(1000);

            //wait for ready with work
            int testCount = 0;
            while (!runnables.isReadyAndEmpty()) {
                sleep(10);
                testCount++;
            }

            System.out.println("TestCount:" + testCount);

            System.out.print(stringBuilder.toString());
            System.out.flush();

            StringBuilder testStringBuilder = new StringBuilder();
            testStringBuilder.append("Start Runnable Runnable 1\n");
            testStringBuilder.append("Finish Runnable Runnable 1\n\n");

            testStringBuilder.append("Start Runnable Runnable 3\n");
            testStringBuilder.append("Finish Runnable Runnable 3\n\n");

            testStringBuilder.append("Start Runnable Runnable 4\n");
            testStringBuilder.append("Finish Runnable Runnable 4\n\n");

            testStringBuilder.append("Start Runnable Runnable 5\n");
            testStringBuilder.append("Finish Runnable Runnable 5\n\n");

            assertEquals(testStringBuilder,stringBuilder,"Runnable1 must start and finish before Runnable3," +
                    " Runnable2 must ignored");
        }
    }

    @Test()
    void runTestwithCancel() {
        if (EXCLUDE_FROM_TRAVIS.VALUE) return;
        synchronized (TestIsRunning) {
            StringBuilder stringBuilder = new StringBuilder();
            ThreadStack<TestCancelRunnable> runnables = new ThreadStack<TestCancelRunnable>();
            TestCancelRunnable runnable1 = new TestCancelRunnable(stringBuilder, "Runnable 1");
            TestCancelRunnable runnable2 = new TestCancelRunnable(stringBuilder, "Runnable 2");
            TestCancelRunnable runnable3 = new TestCancelRunnable(stringBuilder, "Runnable 3");
            TestCancelRunnable runnable4 = new TestCancelRunnable(stringBuilder, "Runnable 4");
            TestCancelRunnable runnable5 = new TestCancelRunnable(stringBuilder, "Runnable 5");

            sleep(100);

            runnables.pushAndStartWithCancelRunning(runnable1);
            sleep(20);
            runnables.pushAndStartWithCancelRunning(runnable2);
            sleep(5);
            runnables.pushAndStartWithCancelRunning(runnable3);
            sleep(100);
            runnables.pushAndStartWithCancelRunning(runnable4);
            sleep(100);
            runnables.pushAndStartWithCancelRunning(runnable5);


            sleep(1000);

            //wait for ready with work
            int testCount = 0;
            while (!runnables.isReadyAndEmpty()) {
                sleep(10);
                testCount++;
            }

            System.out.println("TestCount:" + testCount);
            System.out.print(stringBuilder.toString());
            System.out.flush();

            StringBuilder testStringBuilder = new StringBuilder();
            testStringBuilder.append("Start Runnable Runnable 1\n");
            testStringBuilder.append("Cancel Runnable Runnable 1\n\n");

//            testStringBuilder.append("Start Runnable Runnable 2\n");
//            testStringBuilder.append("Cancel Runnable Runnable 2\n\n");

            testStringBuilder.append("Start Runnable Runnable 3\n");
            testStringBuilder.append("Finish Runnable Runnable 3\n\n");

            testStringBuilder.append("Start Runnable Runnable 4\n");
            testStringBuilder.append("Finish Runnable Runnable 4\n\n");

            testStringBuilder.append("Start Runnable Runnable 5\n");
            testStringBuilder.append("Finish Runnable Runnable 5\n\n");

            assertEquals(testStringBuilder, stringBuilder, "Runnable1 must start and finish before Runnable3," +
                    " Runnable2 must ignored");
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