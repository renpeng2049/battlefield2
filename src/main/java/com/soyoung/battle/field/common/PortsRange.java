package com.soyoung.battle.field.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public class PortsRange {

    private final String portRange;

    public PortsRange(String portRange) {
        this.portRange = portRange;
    }

    public String getPortRangeString() {
        return portRange;
    }

    public int[] ports() throws NumberFormatException {
        final List<Integer> ports = new ArrayList<>();
        iterate(new PortCallback() {
            @Override
            public boolean onPortNumber(int portNumber) {
                ports.add(portNumber);
                return false;
            }
        });

        int[] pi = new int[ports.size()];
        for(int i=0;i<ports.size();i++){
            pi[i] = ports.get(i);
        }
        return pi;
    }

    public boolean iterate(PortCallback callback) throws NumberFormatException {
        StringTokenizer st = new StringTokenizer(portRange, ",");
        boolean success = false;
        while (st.hasMoreTokens() && !success) {
            String portToken = st.nextToken().trim();
            int index = portToken.indexOf('-');
            if (index == -1) {
                int portNumber = Integer.parseInt(portToken.trim());
                success = callback.onPortNumber(portNumber);
                if (success) {
                    break;
                }
            } else {
                int startPort = Integer.parseInt(portToken.substring(0, index).trim());
                int endPort = Integer.parseInt(portToken.substring(index + 1).trim());
                if (endPort < startPort) {
                    throw new IllegalArgumentException("Start port [" + startPort + "] must be greater than end port [" + endPort + "]");
                }
                for (int i = startPort; i <= endPort; i++) {
                    success = callback.onPortNumber(i);
                    if (success) {
                        break;
                    }
                }
            }
        }
        return success;
    }

    public interface PortCallback {
        boolean onPortNumber(int portNumber);
    }

    @Override
    public String toString() {
        return "PortsRange{" +
                "portRange='" + portRange + '\'' +
                '}';
    }
}
