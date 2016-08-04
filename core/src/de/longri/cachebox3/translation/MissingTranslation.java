/*
 * Copyright (C) 2013-2016 team-cachebox.de
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
package de.longri.cachebox3.translation;

/**
 * Extends the Class Translations with hold the ID as String
 * 
 * @author Longri
 */
public class MissingTranslation extends Translations {

	final String stringId;

	public MissingTranslation(String ID, String Trans) {
		super(ID, Trans);
		stringId = ID;
	}

	public String getMissingString() {
		return stringId;
	}

	@Override
	public String toString() {
		return stringId;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MissingTranslation))
			return false;

		MissingTranslation cast = (MissingTranslation) other;

		if (stringId.equals(cast.stringId))
			return true;
		return false;
	}

}
