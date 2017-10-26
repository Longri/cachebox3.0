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
package de.longri.cachebox3.translation.word;

/**
 * Created by Longri on 26.10.2017.
 */
public interface StringSequence extends CharSequence {

    void setHead(StringSequence string);

    void setNext(StringSequence string);

    StringSequence getHead();

    StringSequence getNext();

    StringSequence add(CharSequence string);

    int getSequenceLength();
}
