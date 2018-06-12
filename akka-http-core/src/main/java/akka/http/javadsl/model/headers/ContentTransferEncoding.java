/*
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.http.javadsl.model.headers;

/**
 *  Model for the `Content-Transfer-Encoding` header.
 *  https://www.ietf.org/rfc/rfc2045
 */
public abstract class ContentTransferEncoding extends akka.http.scaladsl.model.HttpHeader {
    public abstract String encoding();

    public static ContentTransferEncoding create(String encoding) {
        return new akka.http.scaladsl.model.headers.Content$minusTransfer$minusEncoding(encoding);
    }
}
