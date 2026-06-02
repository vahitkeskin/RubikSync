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

import math

# Converts RGB (0-255) to CIE L*a*b*
def rgb_to_lab(r, g, b):
    # 1. Convert RGB to XYZ
    r_l = r / 255.0
    g_l = g / 255.0
    b_l = b / 255.0

    r_l = ((r_l + 0.055) / 1.055) ** 2.4 if r_l > 0.04045 else r_l / 12.92
    g_l = ((g_l + 0.055) / 1.055) ** 2.4 if g_l > 0.04045 else g_l / 12.92
    b_l = ((b_l + 0.055) / 1.055) ** 2.4 if b_l > 0.04045 else b_l / 12.92

    r_l *= 100.0
    g_l *= 100.0
    b_l *= 100.0

    x = r_l * 0.4124 + g_l * 0.3576 + b_l * 0.1805
    y = r_l * 0.2126 + g_l * 0.7152 + b_l * 0.0722
    z = r_l * 0.0193 + g_l * 0.1192 + b_l * 0.9505

    # 2. Convert XYZ to CIE L*a*b*
    x_n = x / 95.047
    y_n = y / 100.000
    z_n = z / 108.883

    epsilon = 0.008856
    kappa = 7.787

    x_n = x_n ** (1.0 / 3.0) if x_n > epsilon else (kappa * x_n) + (16.0 / 116.0)
    y_n = y_n ** (1.0 / 3.0) if y_n > epsilon else (kappa * y_n) + (16.0 / 116.0)
    z_n = z_n ** (1.0 / 3.0) if z_n > epsilon else (kappa * z_n) + (16.0 / 116.0)

    l = (116.0 * y_n) - 16.0
    a = 500.0 * (x_n - y_n)
    b = 200.0 * (y_n - z_n)
    return l, a, b

# Calculates the CIEDE2000 distance between two CIE L*a*b* colors.
# Setting k_l = 2.0 reduces the influence of lightness variations (shadows/intensity differences).
def ciede2000(lab1, lab2, k_l=2.0, k_c=1.0, k_h=1.0):
    l1, a1, b1 = lab1
    l2, a2, b2 = lab2

    c1 = math.sqrt(a1 * a1 + b1 * b1)
    c2 = math.sqrt(a2 * a2 + b2 * b2)

    c_bar = (c1 + c2) / 2.0
    c_bar7 = c_bar ** 7.0
    g = 0.5 * (1.0 - math.sqrt(c_bar7 / (c_bar7 + 6103515625.0))) # 25^7 = 6103515625

    a1_prime = a1 * (1.0 + g)
    a2_prime = a2 * (1.0 + g)

    c1_prime = math.sqrt(a1_prime * a1_prime + b1 * b1)
    c2_prime = math.sqrt(a2_prime * a2_prime + b2 * b2)

    c_bar_prime = (c1_prime + c2_prime) / 2.0

    h1_prime = 0.0 if a1_prime == 0.0 and b1 == 0.0 else math.degrees(math.atan2(b1, a1_prime))
    if h1_prime < 0.0:
        h1_prime += 360.0
    h2_prime = 0.0 if a2_prime == 0.0 and b2 == 0.0 else math.degrees(math.atan2(b2, a2_prime))
    if h2_prime < 0.0:
        h2_prime += 360.0

    delta_l_prime = l2 - l1
    delta_c_prime = c2_prime - c1_prime

    h_diff = h2_prime - h1_prime
    if c1_prime * c2_prime == 0.0:
        delta_h_prime = 0.0
    else:
        if abs(h_diff) <= 180.0:
            h_d = h_diff
        elif h_diff > 180.0:
            h_d = h_diff - 360.0
        else:
            h_d = h_diff + 360.0
        delta_h_prime = 2.0 * math.sqrt(c1_prime * c2_prime) * math.sin(math.radians(h_d / 2.0))

    l_bar_prime = (l1 + l2) / 2.0
    if c1_prime * c2_prime == 0.0:
        h_bar_prime = h1_prime + h2_prime
    else:
        sum_h = h1_prime + h2_prime
        if abs(h_diff) <= 180.0:
            h_bar_prime = sum_h / 2.0
        else:
            h_bar_prime = (sum_h + 360.0) / 2.0 if sum_h < 360.0 else (sum_h - 360.0) / 2.0

    t = 1.0 - \
        0.17 * math.cos(math.radians(h_bar_prime - 30.0)) + \
        0.24 * math.cos(math.radians(2.0 * h_bar_prime)) + \
        0.32 * math.cos(math.radians(3.0 * h_bar_prime + 6.0)) - \
        0.20 * math.cos(math.radians(4.0 * h_bar_prime - 63.0))

    s_l = 1.0 + (0.015 * (l_bar_prime - 50.0) ** 2.0) / math.sqrt(20.0 + (l_bar_prime - 50.0) ** 2.0)
    s_c = 1.0 + 0.045 * c_bar_prime
    s_h = 1.0 + 0.015 * c_bar_prime * t

    delta_theta = 30.0 * math.exp(-((h_bar_prime - 275.0) / 25.0) ** 2.0)
    c_bar_prime7 = c_bar_prime ** 7.0
    r_c = 2.0 * math.sqrt(c_bar_prime7 / (c_bar_prime7 + 6103515625.0))
    r_t = -math.sin(math.radians(2.0 * delta_theta)) * r_c

    val_l = delta_l_prime / (k_l * s_l)
    val_c = delta_c_prime / (k_c * s_c)
    val_h = delta_h_prime / (k_h * s_h)

    return math.sqrt(val_l * val_l + val_c * val_c + val_h * val_h + r_t * val_c * val_h)

# Kuhn-Munkres (Hungarian) algorithm implementation for O(N^3) assignment
def hungarian_algorithm(cost_matrix):
    n = len(cost_matrix)
    u = [0.0] * (n + 1)
    v = [0.0] * (n + 1)
    p = [0] * (n + 1)
    way = [0] * (n + 1)
    
    for i in range(1, n + 1):
        p[0] = i
        j0 = 0
        minv = [float('inf')] * (n + 1)
        used = [False] * (n + 1)
        
        while True:
            used[j0] = True
            i0 = p[j0]
            delta = float('inf')
            j1 = 0
            for j in range(1, n + 1):
                if not used[j]:
                    cur = cost_matrix[i0 - 1][j - 1] - u[i0] - v[j]
                    if cur < minv[j]:
                        minv[j] = cur
                        way[j] = j0
                    if minv[j] < delta:
                        delta = minv[j]
                        j1 = j
            for j in range(n + 1):
                if used[j]:
                    u[p[j]] += delta
                    v[j] -= delta
                else:
                    minv[j] -= delta
            j0 = j1
            if p[j0] == 0:
                break
                
        while True:
            j1 = way[j0]
            p[j0] = p[j1]
            j0 = j1
            if j0 == 0:
                break
                
    result = [0] * n
    for j in range(1, n + 1):
        if p[j] > 0:
            result[p[j] - 1] = j - 1
    return result

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
                            rgb = img.getpixel((px, py))
                            # Filter out black borders (gaps between stickers)
                            if not (rgb[0] < 45 and rgb[1] < 45 and rgb[2] < 45):
                                pixels.append(rgb)
                
                if not pixels:
                    # Fallback to simple average including borders
                    for px in range(cx - patch_size, cx + patch_size + 1):
                        for py in range(cy - patch_size, cy + patch_size + 1):
                            if 0 <= px < w and 0 <= py < h:
                                pixels.append(img.getpixel((px, py)))
                
                if not pixels:
                    avg_r, avg_g, avg_b = img.getpixel((cx, cy))
                else:
                    avg_r = sum(p[0] for p in pixels) // len(pixels)
                    avg_g = sum(p[1] for p in pixels) // len(pixels)
                    avg_b = sum(p[2] for p in pixels) // len(pixels)
                
                row_colors.append((avg_r, avg_g, avg_b))
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
        
    try:
        raw_grids = {}
        for face, img_path in face_images.items():
            raw_grids[face] = process_face(img_path)
            
        # Center mapping configuration
        face_to_color = {
            "U": "ORANGE",
            "D": "RED",
            "L": "YELLOW",
            "R": "WHITE",
            "F": "GREEN",
            "B": "BLUE"
        }
        color_to_face = {v: k for k, v in face_to_color.items()}
        
        default_references = {
            "ORANGE": (255, 130, 0),
            "RED": (220, 20, 20),
            "YELLOW": (240, 240, 0),
            "WHITE": (230, 230, 230),
            "GREEN": (0, 160, 0),
            "BLUE": (0, 0, 200)
        }
        
        # Calibrate references from centers in L*a*b*
        ref_lab = {}
        ref_colors = ["ORANGE", "RED", "YELLOW", "WHITE", "GREEN", "BLUE"]
        for color in ref_colors:
            target_face = color_to_face[color]
            raw_face_grid = raw_grids.get(target_face)
            if raw_face_grid:
                ref_rgb = raw_face_grid[1][1]
            else:
                ref_rgb = default_references[color]
            ref_lab[color] = rgb_to_lab(ref_rgb[0], ref_rgb[1], ref_rgb[2])
            
        # Collect 48 non-center cells
        cells_list = []
        for face in FACE_NAMES:
            raw_face_grid = raw_grids[face]
            for r in range(3):
                for c in range(3):
                    if r == 1 and c == 1:
                        continue
                    cells_list.append({
                        "face": face,
                        "row": r,
                        "col": c,
                        "rgb": raw_face_grid[r][c]
                    })
                    
        # Build 48 x 48 cost matrix using CIEDE2000
        cost_matrix = []
        for i in range(48):
            cell = cells_list[i]
            cell_lab = rgb_to_lab(cell["rgb"][0], cell["rgb"][1], cell["rgb"][2])
            row_costs = []
            for j in range(48):
                target_color = ref_colors[j // 8]
                target_lab = ref_lab[target_color]
                dist = ciede2000(cell_lab, target_lab)
                if target_color == "WHITE":
                    white_l = ref_lab["WHITE"][0] if "WHITE" in ref_lab else 90.0
                    if cell_lab[0] < white_l - 22.0:
                        dist += 1000.0
                row_costs.append(dist)
            cost_matrix.append(row_costs)
            
        # Solve matching globally
        assignment = hungarian_algorithm(cost_matrix)
        
        # Format results back to 3x3 grids
        detected_faces = {face: [["" for _ in range(3)] for _ in range(3)] for face in FACE_NAMES}
        
        # Set centers
        for face in FACE_NAMES:
            detected_faces[face][1][1] = face_to_color[face]
            
        # Set outer cells
        for i in range(48):
            cell = cells_list[i]
            assigned_color = ref_colors[assignment[i] // 8]
            detected_faces[cell["face"]][cell["row"]][cell["col"]] = assigned_color
            
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
