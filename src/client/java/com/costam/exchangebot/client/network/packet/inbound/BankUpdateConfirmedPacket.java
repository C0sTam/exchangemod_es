package com.costam.exchangebot.client.network.packet.inbound;


import com.costam.exchangebot.client.util.LoggerUtil;

public class BankUpdateConfirmedPacket implements InboundPacket {
    private double balance;

    public double getBalance() {
        return balance;
    }

    @Override
    public void handle() {
        LoggerUtil.info("Bank update confirmed. New balance: " + balance);
        
    }
}