package com.paperscreen.android.paper.engine

/**
 * The AGSL (Android Graphics Shading Language) program that powers the Paper Rendering Engine.
 * 
 * Modes:
 * 0: ORIGINAL (No effect, or bypass)
 * 1: TWO_TONE (Strict threshold mapping to light/dark)
 * 2: GRAYSCALE (Pure luminance)
 * 3: PAPER (Intermediate shades)
 * 4: ADAPTIVE (Currently disabled/fallback to TWO_TONE)
 */
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
        
        // Mode 0 is ORIGINAL
        if (mode == 0 || strength <= 0.0) {
            return c;
        }
        
        float3 color = float3(c.rgb);
        
        // Apply brightness and contrast
        color = (color - 0.5) * contrast + 0.5 + brightness;
        color = clamp(color, 0.0, 1.0);
        
        float3 finalColor = color;
        float lum = getLuminance(color);
        
        if (mode == 1 || mode == 4) { 
            // 1: TWO_TONE, 4: ADAPTIVE (fallback to TWO_TONE for now)
            float mixFactor = step(threshold, lum);
            finalColor = mix(darkColor, lightColor, mixFactor);
        } else if (mode == 2) {
            // 2: GRAYSCALE
            finalColor = float3(lum);
        } else if (mode == 3) {
            // 3: PAPER (Intermediate shading)
            // Maps the luminance smoothly between darkColor and lightColor
            // applying the threshold as a midpoint shift
            float shiftedLum = clamp(lum + (0.5 - threshold), 0.0, 1.0);
            finalColor = mix(darkColor, lightColor, shiftedLum);
        }
        
        // Apply strength (mix with original)
        finalColor = mix(float3(c.rgb), finalColor, strength);
        
        return half4(float3(finalColor), float(c.a));
    }
"""
