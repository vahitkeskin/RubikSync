import os
import sys
import json
import colorsys

# Try importing Pillow
try:
    from PIL import Image
except ImportError:
    print(json.dumps({
        "status": "error",
        "message": "Pillow kütüphanesi yüklü değil! Lütfen 'pip install Pillow' komutu ile yükleyin."
    }))
    sys.exit(1)

PHOTOS_DIR = "cube_photos"
OUTPUT_FILE = "detected_state.json"

FACE_NAMES = ["U", "D", "L", "R", "F", "B"]
SUPPORTED_EXTENSIONS = [".jpg", ".jpeg", ".png"]

# Classified color mappings to CubeColor string representation
COLOR_MAP = {
    "ORANGE": "ORANGE",
    "RED": "RED",
    "YELLOW": "YELLOW",
    "WHITE": "WHITE",
    "GREEN": "GREEN",
    "BLUE": "BLUE"
}

def classify_color(r, g, b):
    # Normalize RGB to 0-1
    h, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
    h_deg = h * 360.0
    s_pct = s * 100.0
    v_pct = v * 100.0
    
    # White check: very low saturation and high value
    if s_pct < 18 and v_pct > 55:
        return "WHITE"
    
    # Hue-based classification
    if h_deg < 16 or h_deg >= 335:
        return "RED"
    elif 16 <= h_deg < 42:
        return "ORANGE"
    elif 42 <= h_deg < 75:
        return "YELLOW"
    elif 75 <= h_deg < 165:
        return "GREEN"
    elif 165 <= h_deg < 262:
        return "BLUE"
    else:
        # Fallback to closest match
        return "RED"

def find_face_image(face_name):
    for ext in SUPPORTED_EXTENSIONS:
        # Try both upper and lower case
        for name in [face_name, face_name.lower()]:
            file_path = os.path.join(PHOTOS_DIR, f"{name}{ext}")
            if os.path.exists(file_path):
                return file_path
    return None

def process_face(image_path):
    try:
        img = Image.open(image_path).convert("RGB")
        w, h = img.size
        
        # Crop the central square area (65% of the image size)
        crop_ratio = 0.65
        crop_w = int(w * crop_ratio)
        crop_h = int(h * crop_ratio)
        
        left = (w - crop_w) // 2
        top = (h - crop_h) // 2
        right = left + crop_w
        bottom = top + crop_h
        
        img_cropped = img.crop((left, top, right, bottom))
        cw, ch = img_cropped.size
        
        # Grid sizes
        cell_w = cw / 3.0
        cell_h = ch / 3.0
        
        grid = []
        for r in range(3):
            row_colors = []
            for c in range(3):
                # Calculate center of the cell
                cx = int(left + (c + 0.5) * cell_w)
                cy = int(top + (r + 0.5) * cell_h)
                
                # Sample a small 9x9 patch around the cell center to avoid borders
                patch_size = 4
                pixels = []
                for px in range(cx - patch_size, cx + patch_size + 1):
                    for py in range(cy - patch_size, cy + patch_size + 1):
                        if 0 <= px < w and 0 <= py < h:
                            pixels.append(img.getpixel((px, py)))
                
                if not pixels:
                    # Fallback to single pixel
                    avg_r, avg_g, avg_b = img.getpixel((cx, cy))
                else:
                    avg_r = sum(p[0] for p in pixels) // len(pixels)
                    avg_g = sum(p[1] for p in pixels) // len(pixels)
                    avg_b = sum(p[2] for p in pixels) // len(pixels)
                
                color_name = classify_color(avg_r, avg_g, avg_b)
                row_colors.append(color_name)
            grid.append(row_colors)
            
        return grid
    except Exception as e:
        raise ValueError(f"Resim işlenirken hata oluştu ({image_path}): {str(e)}")

def main():
    if not os.path.exists(PHOTOS_DIR):
        os.makedirs(PHOTOS_DIR)
        print(json.dumps({
            "status": "error",
            "message": f"'{PHOTOS_DIR}' klasörü oluşturuldu. Lütfen 6 yüzeyin fotoğrafını ({', '.join(f'{f}.jpg' for f in FACE_NAMES)}) bu klasöre yükleyin."
        }, ensure_ascii=False))
        sys.exit(0)
        
    missing_faces = []
    face_images = {}
    for face in FACE_NAMES:
        img_path = find_face_image(face)
        if img_path:
            face_images[face] = img_path
        else:
            missing_faces.append(face)
            
    if missing_faces:
        print(json.dumps({
            "status": "error",
            "message": f"Eksik fotoğraflar var! Lütfen şu yüzeyleri de yükleyin: {', '.join(missing_faces)}"
        }, ensure_ascii=False))
        sys.exit(0)
        
    detected_faces = {}
    try:
        for face, img_path in face_images.items():
            grid = process_face(img_path)
            # Enforce that the center piece matches the reference center color
            # U->ORANGE, D->RED, L->YELLOW, R->WHITE, F->GREEN, B->BLUE
            centers = {
                "U": "ORANGE",
                "D": "RED",
                "L": "YELLOW",
                "R": "WHITE",
                "F": "GREEN",
                "B": "BLUE"
            }
            grid[1][1] = centers[face] # Force correct center
            detected_faces[face] = grid
            
        # Write success result
        output_data = {
            "status": "success",
            "faces": detected_faces
        }
        with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
            json.dump(output_data, f, ensure_ascii=False, indent=2)
            
        print(json.dumps({
            "status": "success",
            "message": "6 yüzeyin renkleri başarıyla algılandı ve yansıtıldı!"
        }, ensure_ascii=False))
        
    except Exception as e:
        print(json.dumps({
            "status": "error",
            "message": f"İşlem sırasında hata: {str(e)}"
        }, ensure_ascii=False))

if __name__ == "__main__":
    main()
