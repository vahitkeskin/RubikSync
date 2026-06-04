#!/usr/bin/env python3
"""
Rubik's Cube rotation sound generator.
Synthesizes a realistic plastic scraping friction sound followed by a hollow mechanical snap.
"""
import struct
import math
import random
import os

SAMPLE_RATE = 44100
DURATION = 0.08  # 80ms — short, crisp click sound
NUM_SAMPLES = int(SAMPLE_RATE * DURATION)

def generate_rubik_rotate_sound():
    """Synthesizes a clean, sharp plastic click with no friction noise."""
    samples = []
    lpf_click = 0.0
    
    for i in range(NUM_SAMPLES):
        t = i / SAMPLE_RATE
        
        # Hollow plastic core thud (180Hz sine decaying rapidly)
        thud = math.sin(2 * math.pi * 180 * t) * math.exp(-t * 180) * 0.65
        
        # Sharp high-frequency click (decaying almost instantly)
        raw_click_noise = random.uniform(-1.0, 1.0)
        lpf_click = lpf_click + 0.3 * (raw_click_noise - lpf_click)
        click_noise = raw_click_noise - lpf_click  # High frequencies
        click = click_noise * math.exp(-t * 900) * 0.35
        
        # Combine to create the crisp click
        sample = thud + click
        
        # Fade out at the end
        if t > 0.06:
            fade_out = (DURATION - t) / 0.02
            sample *= max(0.0, fade_out)
            
        sample = max(-1.0, min(1.0, sample))
        samples.append(sample)
        
    return samples

def write_wav(filename, samples, sample_rate=44100):
    """Writes a standard mono 16-bit PCM WAV file."""
    num_channels = 1
    bits_per_sample = 16
    byte_rate = sample_rate * num_channels * bits_per_sample // 8
    block_align = num_channels * bits_per_sample // 8
    data_size = len(samples) * block_align
    
    with open(filename, 'wb') as f:
        # RIFF header
        f.write(b'RIFF')
        f.write(struct.pack('<I', 36 + data_size))
        f.write(b'WAVE')
        
        # fmt chunk
        f.write(b'fmt ')
        f.write(struct.pack('<I', 16))  # chunk size
        f.write(struct.pack('<H', 1))   # PCM format
        f.write(struct.pack('<H', num_channels))
        f.write(struct.pack('<I', sample_rate))
        f.write(struct.pack('<I', byte_rate))
        f.write(struct.pack('<H', block_align))
        f.write(struct.pack('<H', bits_per_sample))
        
        # data chunk
        f.write(b'data')
        f.write(struct.pack('<I', data_size))
        
        for sample in samples:
            value = int(sample * 32767)
            value = max(-32768, min(32767, value))
            f.write(struct.pack('<h', value))


if __name__ == '__main__':
    random.seed(42)  # Fixed seed for deterministic build output
    samples = generate_rubik_rotate_sound()
    
    wav_path = '/Users/vahitkeskin/AndroidStudioProjects/RubikSync/androidApp/src/main/res/raw/cube_rotate.wav'
    write_wav(wav_path, samples)
    print(f"WAV created at: {wav_path}")
    print(f"Duration: {DURATION*1000:.0f}ms, Samples: {len(samples)}")
