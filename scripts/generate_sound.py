#!/usr/bin/env python3
"""
Rubik küpü döndürme sesi oluşturucu.
Gerçekçi bir plastik tıklama + sürtünme sesi sentezler.
"""
import struct
import math
import random
import os

SAMPLE_RATE = 44100
DURATION = 0.18  # 180ms — kısa, keskin bir döndürme sesi
NUM_SAMPLES = int(SAMPLE_RATE * DURATION)

def generate_rubik_rotate_sound():
    """Plastik mekanizma tıklaması + sürtünme sesi sentezler."""
    samples = []
    
    for i in range(NUM_SAMPLES):
        t = i / SAMPLE_RATE
        progress = i / NUM_SAMPLES  # 0.0 → 1.0
        
        # 1. Başlangıç tıklaması — keskin, kısa darbe (ilk 15ms)
        click = 0.0
        if t < 0.015:
            click_env = math.exp(-t * 300)
            click = click_env * (
                math.sin(2 * math.pi * 2200 * t) * 0.4 +
                math.sin(2 * math.pi * 3800 * t) * 0.25 +
                math.sin(2 * math.pi * 5500 * t) * 0.15
            )
        
        # 2. Plastik sürtünme gürültüsü — band-limited noise
        noise = 0.0
        friction_env = math.exp(-t * 18) * (1 - math.exp(-t * 200))
        for freq in [1800, 2600, 3400, 4200]:
            phase = random.uniform(0, 2 * math.pi) if i == 0 else 0
            noise += math.sin(2 * math.pi * freq * t + phase * (1 + 0.3 * math.sin(t * 50)))
        noise *= friction_env * 0.08
        
        # 3. Mekanizma vurma sesi — düşük frekanslı thud
        thud = 0.0
        if t < 0.03:
            thud_env = math.exp(-t * 150)
            thud = thud_env * math.sin(2 * math.pi * 400 * t) * 0.3
        
        # 4. Bitiş tıklaması — yerine oturma (son 20ms)
        end_click = 0.0
        end_t = DURATION - t
        if end_t < 0.02 and end_t > 0:
            end_env = math.exp(-end_t * 200) * 0.5
            end_click = end_env * (
                math.sin(2 * math.pi * 2800 * t) * 0.3 +
                math.sin(2 * math.pi * 4500 * t) * 0.2
            )
        
        # 5. Rastgele mikro tıklamalar (plastik dişliler)
        micro_clicks = 0.0
        for click_time in [0.04, 0.07, 0.10, 0.13]:
            dt = abs(t - click_time)
            if dt < 0.003:
                micro_env = math.exp(-dt * 800)
                micro_clicks += micro_env * math.sin(2 * math.pi * 3200 * t) * 0.1
        
        # Tüm bileşenleri birleştir
        sample = click + noise + thud + end_click + micro_clicks
        
        # Genel zarf — yumuşak fade out
        master_env = 1.0 - progress ** 3
        sample *= master_env * 0.7
        
        # -1.0 ile 1.0 arasında sınırla
        sample = max(-1.0, min(1.0, sample))
        samples.append(sample)
    
    return samples


def write_wav(filename, samples, sample_rate=44100):
    """PCM WAV dosyası yazar."""
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
    random.seed(42)  # Tutarlı sonuç için sabit seed
    samples = generate_rubik_rotate_sound()
    
    wav_path = '/Users/vahitkeskin/AndroidStudioProjects/RubikSync/androidApp/src/main/res/raw/cube_rotate.wav'
    write_wav(wav_path, samples)
    print(f"WAV oluşturuldu: {wav_path}")
    print(f"Süre: {DURATION*1000:.0f}ms, Örnekler: {len(samples)}")
