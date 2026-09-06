import sys
import time
import subprocess
import ctypes

cg = ctypes.cdll.LoadLibrary('/System/Library/Frameworks/CoreGraphics.framework/CoreGraphics')

class CGPoint(ctypes.Structure):
    _fields_ = [('x', ctypes.c_double), ('y', ctypes.c_double)]

cg.CGEventCreateMouseEvent.argtypes = [ctypes.c_void_p, ctypes.c_uint32, CGPoint, ctypes.c_uint32]
cg.CGEventCreateMouseEvent.restype = ctypes.c_void_p
cg.CGEventPost.argtypes = [ctypes.c_uint32, ctypes.c_void_p]
cg.CFRelease.argtypes = [ctypes.c_void_p]

def get_screen_bounds():
    cmd = "osascript -e 'tell application \"Simulator\" to activate' -e 'tell application \"System Events\" to tell process \"Simulator\" to tell window 1 to get {position, size} of group 1'"
    out = subprocess.check_output(cmd, shell=True).decode().strip()
    parts = [int(p.strip()) for p in out.split(',')]
    return parts[0], parts[1], parts[2], parts[3]

def click_norm(u, v):
    x0, y0, w, h = get_screen_bounds()
    target_x = x0 + u * w
    target_y = y0 + v * h
    pt = CGPoint(target_x, target_y)
    
    down = cg.CGEventCreateMouseEvent(None, 1, pt, 0)
    up = cg.CGEventCreateMouseEvent(None, 2, pt, 0)
    cg.CGEventPost(0, down)
    time.sleep(0.08)
    cg.CGEventPost(0, up)
    cg.CFRelease(down)
    cg.CFRelease(up)
    time.sleep(0.5)

def drag_norm(u1, v1, u2, v2, steps=15):
    x0, y0, w, h = get_screen_bounds()
    sx, sy = x0 + u1 * w, y0 + v1 * h
    ex, ey = x0 + u2 * w, y0 + v2 * h
    
    start_pt = CGPoint(sx, sy)
    down = cg.CGEventCreateMouseEvent(None, 1, start_pt, 0)
    cg.CGEventPost(0, down)
    time.sleep(0.05)
    
    for i in range(1, steps + 1):
        cx = sx + (ex - sx) * (i / steps)
        cy = sy + (ey - sy) * (i / steps)
        mv = cg.CGEventCreateMouseEvent(None, 6, CGPoint(cx, cy), 0)
        cg.CGEventPost(0, mv)
        cg.CFRelease(mv)
        time.sleep(0.02)
        
    end_pt = CGPoint(ex, ey)
    up = cg.CGEventCreateMouseEvent(None, 2, end_pt, 0)
    cg.CGEventPost(0, up)
    cg.CFRelease(down)
    cg.CFRelease(up)
    time.sleep(0.5)

def capture_screenshot(out_path):
    subprocess.run(["xcrun", "simctl", "io", "2A43E2C9-68ED-4962-A15B-9F0B2D5B4BA0", "screenshot", out_path], check=True)

if __name__ == '__main__':
    action = sys.argv[1]
    if action == 'click':
        u = float(sys.argv[2])
        v = float(sys.argv[3])
        click_norm(u, v)
    elif action == 'drag':
        u1 = float(sys.argv[2])
        v1 = float(sys.argv[3])
        u2 = float(sys.argv[4])
        v2 = float(sys.argv[5])
        drag_norm(u1, v1, u2, v2)
    elif action == 'screenshot':
        capture_screenshot(sys.argv[2])
