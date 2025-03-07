/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file and, per its terms, should not be removed:
 *
 * Copyright (c) 2004 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

package org.w3c.dom.ls;

/**
 *  Parser or write operations may throw an <code>LSException</code> if the
 * processing is stopped. The processing can be stopped due to a
 * <code>DOMError</code> with a severity of
 * <code>DOMError.SEVERITY_FATAL_ERROR</code> or a non recovered
 * <code>DOMError.SEVERITY_ERROR</code>, or if
 * <code>DOMErrorHandler.handleError()</code> returned <code>false</code>.
 * <p ><b>Note:</b>  As suggested in the definition of the constants in the
 * <code>DOMError</code> interface, a DOM implementation may choose to
 * continue after a fatal error, but the resulting DOM tree is then
 * implementation dependent.
 * <p>See also the <a href='http://www.w3.org/TR/2004/REC-DOM-Level-3-LS-20040407'>Document Object Model (DOM) Level 3 Load
and Save Specification</a>.
 *
 * @since 1.5
 */
public class LSException extends RuntimeException {
    private static final long serialVersionUID = 5371691160978884690L;

    public LSException(short code, String message) {
       super(message);
       this.code = code;
    }
    /**
     * @serial
     */
    public short   code;
    // LSExceptionCode
    /**
     *  If an attempt was made to load a document, or an XML Fragment, using
     * <code>LSParser</code> and the processing has been stopped.
     */
    public static final short PARSE_ERR                 = 81;
    /**
     *  If an attempt was made to serialize a <code>Node</code> using
     * <code>LSSerializer</code> and the processing has been stopped.
     */
    public static final short SERIALIZE_ERR             = 82;

}
