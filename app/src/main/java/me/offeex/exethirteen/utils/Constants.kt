/*******************************************************************************
 *                                                                             *
 *  Copyright (C) 2017 by Max Lv <max.c.lv@gmail.com>                          *
 *  Copyright (C) 2017 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                             *
 *  This program is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by       *
 *  the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                        *
 *                                                                             *
 *  This program is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 *  GNU General Public License for more details.                               *
 *                                                                             *
 *  You should have received a copy of the GNU General Public License          *
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                             *
 *******************************************************************************/

package me.offeex.exethirteen.utils

object Key {
    /**
     * Public config that doesn't need to be kept secret.
     */
    const val DB_PUBLIC = "config.db"

    const val serviceMode = "serviceMode"
    const val shareOverLan = "shareOverLan"
    const val portProxy = "portProxy"
    const val portLocalDns = "portLocalDns"

    const val assetUpdateTime = "assetUpdateTime"
}

object Action {
    const val SERVICE = "me.offeex.exethirteen.SERVICE"
    const val CLOSE = "me.offeex.exethirteen.CLOSE"
    const val RELOAD = "me.offeex.exethirteen.RELOAD"
    const val ABORT = "me.offeex.exethirteen.ABORT"
}
