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
DURATION = 0.24  # 240ms — optimal duration for a layer rotation
NUM_SAMPLES = int(SAMPLE_RATE * DURATION)

def generate_rubik_rotate_sound():
    """Synthesizes a realistic plastic friction + hollow core snap."""
    samples = []
    lpf_friction = 0.0
    lpf_click = 0.0
    
    # Position the alignment snap click at ~110ms into the turn
    snap_sample = int(SAMPLE_RATE * 0.11)
    
    for i in range(NUM_SAMPLES):
        t = i / SAMPLE_RATE
        
        # 1. Plastic Friction (Low-pass filtered white noise for scraping sound)
        raw_noise = random.uniform(-1.0, 1.0)
        # alpha = 0.075 (~520Hz cutoff) creates a deep, warm plastic rubbing sound
        lpf_friction = lpf_friction + 0.075 * (raw_noise - lpf_friction)
        
        # Friction envelope: bell-shaped over the turn duration
        friction_env = math.sin(t * math.pi / DURATION) ** 1.4
        friction = lpf_friction * friction_env * 0.32
        
        # 2. Alignment snap click
        snap = 0.0
        if i >= snap_sample:
            t_snap = (i - snap_sample) / SAMPLE_RATE
            
            # Hollow plastic core thud (160Hz sine decaying rapidly)
            thud = math.sin(2 * math.pi * 160 * t_snap) * math.exp(-t_snap * 110) * 0.45
            
            # Sharp tooth contact click (High-pass filtered noise decaying instantly)
            raw_click_noise = random.uniform(-1.0, 1.0)
            lpf_click = lpf_click + 0.28 * (raw_click_noise - lpf_click)
            click_noise = raw_click_noise - lpf_click  # High frequencies only
            click = click_noise * math.exp(-t_snap * 480) * 0.22
            
            snap = thud + click
            
        # Combine friction and snap click
        sample = friction + snap
        
        # Master envelope with slight fade-out at the very end
        if t > 0.21:
            fade_out = (DURATION - t) / 0.03
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
