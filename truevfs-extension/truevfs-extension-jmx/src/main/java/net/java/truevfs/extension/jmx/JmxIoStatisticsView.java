/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.truevfs.extension.jmx;

import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.*;

/**
 * Provides statistics for the federated file systems managed by a single file
 * system manager.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
public class JmxIoStatisticsView
extends StandardMBean
implements JmxIoStatisticsMXBean {

    private static final MBeanServer
            mbs = ManagementFactory.getPlatformMBeanServer();

    private final JmxIoStatistics stats;
    private final String type;

    static synchronized JmxIoStatisticsMXBean register(final JmxIoStatistics model, final String type) {
        final JmxIoStatisticsView view = new JmxIoStatisticsView(model, type);
        final ObjectName name = getObjectName(model, type);
        try {
            try {
                mbs.registerMBean(view, name);
                return view;
            } catch (InstanceAlreadyExistsException ignored) {
                return JMX.newMXBeanProxy(mbs, name, JmxIoStatisticsMXBean.class);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    static void unregister(final JmxIoStatistics model, final String type) {
        final ObjectName name = getObjectName(model, type);
        try {
            try {
                mbs.unregisterMBean(name);
            } catch (InstanceNotFoundException ignored) {
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private static synchronized ObjectName getObjectName(
            final JmxIoStatistics model,
            final String type) {
        final long time = model.getTimeCreatedMillis();
        try {
            return new ObjectName(String.format("%s:time=%s,type=%s",
                    JmxIoStatisticsView.class.getPackage().getName(),
                    ObjectName.quote(format(time)),
                    Objects.requireNonNull(type)));
        } catch (MalformedObjectNameException ex) {
            throw new AssertionError(ex);
        }
    }

    private JmxIoStatisticsView(final JmxIoStatistics stats, final String type) {
        super(JmxIoStatisticsMXBean.class, true);
        assert null != stats;
        assert null != type;
        this.stats = stats;
        this.type = type;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        MBeanInfo mbinfo = super.getMBeanInfo();
        return new MBeanInfo(mbinfo.getClassName(),
                mbinfo.getDescription(),
                mbinfo.getAttributes(),
                mbinfo.getConstructors(),
                mbinfo.getOperations(),
                getNotificationInfo());
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[]{};
    }

    /**
     * Override customization hook:
     * You can supply a customized description for MBeanInfo.getDescription()
     */
    @Override
    protected String getDescription(MBeanInfo info) {
        return "A record of I/O statistics.";
    }

    /**
     * Override customization hook:
     * You can supply a customized description for MBeanAttributeInfo.getDescription()
     */
    @Override
    protected String getDescription(MBeanAttributeInfo info) {
        String description = null;
        switch (info.getName()) {
        case "Type":
            description = "The type of these I/O statistics.";
            break;
        case "TimeCreated":
            description = "The time these I/O statistics have been created.";
            break;
        case "Read":
            description = "The number of bytes read.";
            break;
        case "Written":
            description = "The number of bytes written.";
            break;
        }
        return description;
    }

    /**
     * Override customization hook:
     * You can supply a customized description for MBeanParameterInfo.getDescription()
     */
    @Override
    protected String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        return null;
    }

    /**
     * Override customization hook:
     * You can supply a customized description for MBeanParameterInfo.getType()
     */
    @Override
    protected String getParameterName(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        return null;
    }

    /**
     * Override customization hook:
     * You can supply a customized description for MBeanOperationInfo.getDescription()
     */
    @Override
    protected String getDescription(MBeanOperationInfo info) {
        String description = null;
        if (info.getName().equals("close")) {
            description = "Closes these I/O statistics log.";
        }
        return description;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getTimeCreated() {
        return format(stats.getTimeCreatedMillis());
    }

    private static String format(long time) {
        return DateFormat.getDateTimeInstance().format(new Date(time));
    }

    @Override
    public long getTimeCreatedMillis() {
        return stats.getTimeCreatedMillis();
    }

    @Override
    public long getBytesRead() {
        return stats.getBytesRead();
    }

    @Override
    public long getBytesWritten() {
        return stats.getBytesWritten();
    }

    @Override
    public void close() {
        unregister(stats, type);
    }
}