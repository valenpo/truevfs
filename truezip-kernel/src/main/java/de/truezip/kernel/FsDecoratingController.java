/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.truezip.kernel;

import de.truezip.kernel.cio.Entry;
import de.truezip.kernel.cio.Entry.Access;
import de.truezip.kernel.cio.Entry.Type;
import de.truezip.kernel.cio.InputSocket;
import de.truezip.kernel.cio.OutputSocket;
import de.truezip.kernel.util.BitField;
import java.io.IOException;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An abstract decorator for a file system controller.
 *
 * @param  <M> the type of the file system model.
 * @param  <C> the type of the decorated file system controller.
 * @author Christian Schlichtherle
 */
@ThreadSafe
public abstract class FsDecoratingController<
        M extends FsModel,
        C extends FsController<? extends M>>
extends FsModelController<M> {

    /** The decorated file system controller. */
    protected final C controller;

    protected FsDecoratingController(final C controller) {
        super(controller.getModel());
        this.controller = controller;
    }

    @Override
    public FsController<?> getParent() {
        return controller.getParent();
    }

    @Override
    public boolean isReadOnly() throws IOException {
        return controller.isReadOnly();
    }

    @Override
    public @Nullable FsEntry stat(
            BitField<FsAccessOption> options,
            FsEntryName name)
    throws IOException {
        return controller.stat(options, name);
    }

    @Override
    public void checkAccess(
            BitField<FsAccessOption> options,
            FsEntryName name,
            BitField<Access> types)
    throws IOException {
        controller.checkAccess(options, name, types);
    }

    @Override
    public void setReadOnly(FsEntryName name) throws IOException {
        controller.setReadOnly(name);
    }

    @Override
    public boolean setTime(
            BitField<FsAccessOption> options,
            FsEntryName name,
            Map<Access, Long> times)
    throws IOException {
        return controller.setTime(options, name, times);
    }

    @Override
    public boolean setTime(
            BitField<FsAccessOption> options,
            FsEntryName name,
            BitField<Access> types,
            long value)
    throws IOException {
        return controller.setTime(options, name, types, value);
    }

    @Override
    public InputSocket<?> input(
            BitField<FsAccessOption> options,
            FsEntryName name) {
        return controller.input(options, name);
    }

    @Override
    public OutputSocket<?> output(
            BitField<FsAccessOption> options,
            FsEntryName name,
            Entry template) {
        return controller.output(options, name, template);
    }

    @Override
    public void mknod(
            BitField<FsAccessOption> options,
            FsEntryName name,
            Type type,
            Entry template)
    throws IOException {
        controller.mknod(options, name, type, template);
    }

    @Override
    public void unlink(
            BitField<FsAccessOption> options,
            FsEntryName name)
    throws IOException {
        controller.unlink(options, name);
    }

    @Override
    public void sync(BitField<FsSyncOption> options)
    throws FsSyncWarningException, FsSyncException {
        controller.sync(options);
    }

    @Override
    public String toString() {
        return String.format("%s[controller=%s]",
                getClass().getName(),
                controller);
    }
}
