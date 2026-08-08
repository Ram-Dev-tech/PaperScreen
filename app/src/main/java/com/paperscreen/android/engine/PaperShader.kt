package com.paperscreen.android.engine

const val PAPER_SHADER = """
    uniform shader content;
    uniform float3 lightColor;
    uniform float3 darkColor;
    uniform float brightness;
    uniform float contrast;
    uniform float strength;
    uniform float threshold;
    uniform int mode;
    
    float getLuminance(float3 color) {
        return dot(color, float3(0.299, 0.587, 0.114));
    }
    
    half4 main(float2 coord) {
        half4 c = content.eval(coord);
        
        if (mode == 0 || strength <= 0.0) {
            return c;
        }
        
        float3 color = float3(c.rgb);
        
        color = (color - 0.5) * contrast + 0.5 + brightness;
        color = clamp(color, 0.0, 1.0);
        
        float3 finalColor = color;
        
        if (mode == 1) {
            float lum = getLuminance(color);
            float mixFactor = step(threshold, lum);
            finalColor = mix(darkColor, lightColor, mixFactor);
        } else if (mode == 2) {
            float lum = getLuminance(color);
            finalColor = float3(lum);
        }
        
        finalColor = mix(float3(c.rgb), finalColor, strength);
        
        return half4(float3(finalColor), float(c.a));
    }
"""
