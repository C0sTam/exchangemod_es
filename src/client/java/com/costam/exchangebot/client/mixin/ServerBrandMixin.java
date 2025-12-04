package com.costam.exchangebot.client.mixin;

import com.costam.exchangebot.client.util.ServerInfoUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(ClientPlayNetworkHandler.class)
public class ServerBrandMixin {

    @Unique
    private String lastBrand = null;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientPlayNetworkHandler handler = (ClientPlayNetworkHandler) (Object) this;
        String currentBrand = handler.getBrand();

        if (currentBrand != null && !currentBrand.equals(lastBrand)) {
            lastBrand = currentBrand;
            String cleaned = currentBrand.replaceAll("§.", "");
            String currentAddress = ServerInfoUtil.getServerAddress().toLowerCase();
            cleaned = cleaned.replaceAll("(?i)\\s*\\|?\\s*" + Pattern.quote(currentAddress), "");

            
            cleaned = cleaned.replaceAll("(?i)\\s*\\(velocity\\)\\s*", "");

            
            cleaned = cleaned.replaceAll("\\s*\\|\\s*", "").trim();
            ServerInfoUtil.setServerBrand(cleaned);

            System.out.println("[Server Brand] " + currentBrand);
        }
    }
}