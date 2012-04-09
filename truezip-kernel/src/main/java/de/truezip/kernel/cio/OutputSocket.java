/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.truezip.kernel.cio;

import de.truezip.kernel.io.ChannelOutputStream;
import de.truezip.kernel.io.Sink;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * An abstract factory for output resources for writing bytes to its
 * <i>local target</i>.
 * <p>
 * Note that the entity relationship between output sockets and input sockets
 * is n:1, i.e. any output socket can have at most one remote input socket, but
 * it may be the remote of many other input sockets.
 *
 * @param  <E> the type of the {@link #getLocalTarget() local target}
 *         for I/O operations.
 * @see    InputSocket
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public abstract class OutputSocket<E extends Entry>
extends IOSocket<E, Entry> implements Sink {

    @CheckForNull
    private InputSocket<?> remote;

    /**
     * {@inheritDoc}
     * <p>
     * The remote target is {@code null} if and only if this socket is not
     * {@link #connect}ed to another socket.
     * 
     * @throws IOException On any I/O error.
     */
    // See https://java.net/jira/browse/TRUEZIP-203
    @Override
    public final @Nullable Entry getRemoteTarget() throws IOException {
        return null == remote ? null : remote.getLocalTarget();
    }

    /**
     * Binds this socket to given socket by inheriting its
     * {@link #getRemoteTarget() remote target}.
     * Note that this method does <em>not</em> change the state of the
     * given socket and does <em>not</em> connect this socket to the remote
     * socket, that is it does not set this socket as the remote of of the given
     * socket.
     *
     * @param  to the output socket with the remote target to inherit.
     * @return {@code this}
     * @throws IllegalArgumentException if {@code this} == {@code to}.
     */
    public final OutputSocket<E> bind(final OutputSocket<?> to) {
        if (this == to)
            throw new IllegalArgumentException();
        this.remote = to.remote;
        return this;
    }

    /**
     * Connects this output socket to the given remote input socket.
     * Note that this method changes the remote output socket of
     * the given remote input socket to this instance.
     *
     * @param  newRemote the nullable remote input socket to connect to.
     * @return {@code this}
     */
    final OutputSocket<E> connect(@CheckForNull final InputSocket<?> newRemote) {
        final InputSocket<?> oldRemote = remote;
        if (oldRemote != newRemote) {
            if (null != oldRemote) {
                remote = null;
                oldRemote.connect(null);
            }
            if (null != newRemote) {
                remote = newRemote;
                newRemote.connect(this);
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementations must support calling this method multiple times.
     * <p>
     * The implementation in the class {@link OutputSocket} calls
     * {@link #newChannel()} and wraps the result in a
     * {@link ChannelOutputStream} adapter.
     * Note that this may intentionally violate the contract for this method
     * because {@link #newChannel()} may throw an
     * {@link UnsupportedOperationException} while this method may not,
     * so override appropriately.
     */
    @Override
    public OutputStream newStream() throws IOException {
        return new ChannelOutputStream(newChannel());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementations must support calling this method multiple times.
     * 
     * @throws UnsupportedOperationException the implementation in the class
     *         {@link OutputSocket} <em>always</em> throws an exception of
     *         this type.
     */
    @Override
    public SeekableByteChannel newChannel() throws IOException {
        throw new UnsupportedOperationException();
    }
}
