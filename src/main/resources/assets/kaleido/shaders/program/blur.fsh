#version 110

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float Radius;
uniform vec4 Area;

void main() {
    if (texCoord.x < Area.x || texCoord.y < Area.y || texCoord.x > Area.z || texCoord.y > Area.w) {
        gl_FragColor = texture2D(DiffuseSampler, texCoord);
    } else {
        vec4 blurred = vec4(0.0);
        float totalStrength = 0.0;
        float totalAlpha = 0.0;
        float totalSamples = 0.0;
        float step = Radius / 5.0;
        for(float r = -Radius; r <= Radius; r += step) {
            vec4 sampleValue = texture2D(DiffuseSampler, texCoord + oneTexel * r * BlurDir);
    
            // Accumulate average alpha
            totalAlpha = totalAlpha + sampleValue.a;
            // totalSamples = totalSamples + 1.0;
    
            // Accumulate smoothed blur
            float strength = 1.0 - abs(r / Radius);
            totalStrength = totalStrength + strength;
            blurred = blurred + sampleValue;
        }
        gl_FragColor = vec4(blurred.rgb / 11.0, totalAlpha);
    }
}
