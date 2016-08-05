/* 
 * Copyright (C) 2016 team-cachebox.de
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
package de.longri.cachebox3.utils;

import java.io.Serializable;

/**
 * DevicesSizes ist eine Struktur die alle wichtigen geräteabhängigen Größen enthält. Sie wird in der Regel der Klasse UI_Size_Base im Constructor
 * übergeben, damit die Größen in dieser berechnet werden können!
 * 
 * @author Longri
 */
public class DevicesSizes implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5441749943486589905L;

	/**
	 * Die Größe die CB im gesamten zur Verfügung steht.
	 */
	public SizeF Window;

	/**
	 * die Auflösung die das Display hat
	 */
	public float Density;

	/**
	 * True wenn die Berechnung für Landscape durchgeführt werden soll.
	 */
	public boolean isLandscape;
}
