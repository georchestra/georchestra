/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.mapfishapp.ws;

/**
 * This exception should be used to send specific HTTP error to client. <br />
 * It is strongly tied with HTTP codes from {@link javax.servlet.http.HttpServletResponse}
 * @author yoann.buch@gmail.com
 *
 */

@SuppressWarnings("serial")
public class DocServiceException extends Exception{

    private int _errorCode;
    
    /**
     * Constructor
     * @param message String Exception message 
     * @param code HTTP error code. Can be retrieved from HttpServletResponse
     */
    public DocServiceException(String message, int code) {
        super(message);
        _errorCode = code;
    }
    
    /**
     * Get error code
     * @return int error code
     */
    public int getErrorCode() {
        return _errorCode;
    }
}
