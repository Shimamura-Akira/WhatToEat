package com.example.whattoeat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

public class RouletteView extends View {
    private Paint paintArc, paintText, paintPointer, paintBorder, paintCenter;
    private RectF rectF;
    private List<String> dataList = new ArrayList<>();
    private float currentRotation = 0f;
    private ValueAnimator animator;
    private SpinListener spinListener;
    private int lastTick = 0;

    public interface SpinListener {
        void onSpinEnd(String result);
    }

    public RouletteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null); // 启用硬件加速阴影支持

        paintArc = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintArc.setStyle(Paint.Style.FILL);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.parseColor("#333333"));
        paintText.setTextSize(50f);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        // 给文字加一点淡白色发光，提升在彩色盘面上的立体感和可读性
        paintText.setShadowLayer(3f, 0f, 2f, Color.WHITE);

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setColor(Color.WHITE);
        paintBorder.setStrokeWidth(6f);

        paintPointer = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPointer.setColor(0xFFE53935); // 更有质感的红色
        paintPointer.setStyle(Paint.Style.FILL);
        paintPointer.setShadowLayer(10f, 0f, 5f, 0x66000000); // 指针投影

        paintCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCenter.setStyle(Paint.Style.FILL);
        paintCenter.setColor(Color.WHITE);
        paintCenter.setShadowLayer(12f, 0f, 6f, 0x44000000); // 轴心投影

        rectF = new RectF();
    }

    public void setData(List<String> data) {
        this.dataList = data;
        invalidate();
    }

    public void setSpinListener(SpinListener listener) {
        this.spinListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int padding = 60;
        int size = Math.min(w, h) - padding * 2;
        int left = (w - size) / 2;
        int top = (h - size) / 2;
        rectF.set(left, top, left + size, top + size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataList == null || dataList.isEmpty()) {
            paintText.setTextSize(40f);
            canvas.drawText("请添加选项", getWidth() / 2f, getHeight() / 2f, paintText);
            return;
        }

        int size = dataList.size();
        float sweepAngle = 360f / size;

        // --- 1. 画转盘的最底层（托盘底座阴影） ---
        paintArc.setColor(Color.WHITE);
        paintArc.setShadowLayer(20f, 0f, 10f, 0x44000000);
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.width() / 2f, paintArc);
        paintArc.clearShadowLayer();

        canvas.save();
        canvas.rotate(currentRotation, rectF.centerX(), rectF.centerY());

        for (int i = 0; i < size; i++) {
            // --- 2. 画彩色扇形 ---
            paintArc.setColor(getArcColor(i, size));
            canvas.drawArc(rectF, i * sweepAngle, sweepAngle, true, paintArc);

            // --- 3. 画扇形之间的边界线（描边） ---
            canvas.drawArc(rectF, i * sweepAngle, sweepAngle, true, paintBorder);

            // --- 4. 扇形内对齐画文字 ---
            canvas.save();
            // 旋转画布中心，使得文字绘制沿着当前扇形的对称轴向外发散
            float angle = i * sweepAngle + sweepAngle / 2;
            canvas.rotate(angle, rectF.centerX(), rectF.centerY());
            
            // 往外靠一点的位置
            float textX = rectF.centerX() + rectF.width() / 2 * 0.6f;
            float textY = rectF.centerY() - (paintText.descent() + paintText.ascent()) / 2;
            
            // Adjust text size dynamically based on length
            String text = dataList.get(i);
            paintText.setTextSize(40f);
            
            // Measure text width and available width
            float maxWidth = rectF.width() / 2 * 0.55f; // Leave some padding
            if (paintText.measureText(text) > maxWidth) {
                // If still too long, truncate with ellipsis
                CharSequence truncatedText = android.text.TextUtils.ellipsize(
                        text, 
                        new android.text.TextPaint(paintText), 
                        maxWidth, 
                        android.text.TextUtils.TruncateAt.END);
                canvas.drawText(truncatedText.toString(), textX, textY, paintText);
            } else {
                canvas.drawText(text, textX, textY, paintText);
            }
            
            canvas.restore();
        }
        
        // --- 5. 画外圆环（边框锁定感） ---
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.width() / 2f, paintBorder);
        canvas.restore();

        // --- 6. 绘制顶部质感指针 (带长阴影) ---
        Path path = new Path();
        path.moveTo(rectF.centerX() - 35, rectF.top - 40); // 顶部宽一些
        path.lineTo(rectF.centerX() + 35, rectF.top - 40);
        path.lineTo(rectF.centerX(), rectF.top + 35); // 箭头深入盘内
        path.close();
        canvas.drawPath(path, paintPointer);
        
        // --- 6.1 指针根部的金属扣钉 ---
        paintPointer.setColor(0xFFB71C1C); // 深红点
        paintPointer.clearShadowLayer();
        canvas.drawCircle(rectF.centerX(), rectF.top - 30, 8f, paintPointer);
        
        // 恢复指针画笔的状态供下一帧使用
        paintPointer.setColor(0xFFE53935);
        paintPointer.setShadowLayer(10f, 0f, 5f, 0x66000000);

        // --- 7. 画中心高光圆盘（双层立体感） ---
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), 45f, paintCenter);
        paintCenter.setColor(0xFFF5F5F5); // 第二层内圈微灰
        paintCenter.clearShadowLayer();
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), 25f, paintCenter);

        // 恢复画笔原本的白色带阴影供下一帧
        paintCenter.setColor(Color.WHITE);
        paintCenter.setShadowLayer(12f, 0f, 6f, 0x44000000);
    }

    private int getArcColor(int index, int total) {
        // Pastel Material 3 style colors
        int[] predefined = {
            0xFFFFCDD2, 0xFFBBDEFB, 0xFFC8E6C9, 0xFFFFF9C4,
            0xFFE1BEE7, 0xFFFFE0B2, 0xFFB2EBF2, 0xFFDCEDC8
        };
        // avoid identical adjacent colors on odd counts
        if (total % 2 != 0 && index == total - 1 && total > 1) {
            return predefined[2]; 
        }
        return predefined[index % predefined.length];
    }

    public void spin(int targetIndex) {
        if (animator != null && animator.isRunning()) return;
        if (dataList.isEmpty()) return;

        int size = dataList.size();
        float sweepAngle = 360f / size;
        
        // We want the target slice center to land at 270 degrees (Top)
        // Item angle starts at (targetIndex * sweepAngle + sweepAngle / 2)
        // targetRotation + itemAngle = 270 (mod 360)
        float targetAngle = 270f - (targetIndex * sweepAngle + sweepAngle / 2);
        
        // Spin multiple full rounds (e.g. 5 times)
        float totalRotation = currentRotation + (360f * 5) + ((targetAngle - currentRotation) % 360f);
        
        // Ensure minimum spin
        if (totalRotation < currentRotation + 360f * 4) {
            totalRotation += 360f;
        }

        lastTick = (int) (currentRotation / sweepAngle);

        animator = ValueAnimator.ofFloat(currentRotation, totalRotation);
        animator.setDuration(4000); // 4 seconds spin for tension
        animator.setInterpolator(new DecelerateInterpolator(1.5f));
        animator.addUpdateListener(animation -> {
            currentRotation = (float) animation.getAnimatedValue();
            
            // Play tick sound/haptic when crossing a boundary
            int currentTick = (int) ((currentRotation - 270f) / sweepAngle);
            if (currentTick != lastTick) {
                performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                lastTick = currentTick;
            }
            
            invalidate();
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentRotation = currentRotation % 360f;
                performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                if (spinListener != null) {
                    spinListener.onSpinEnd(dataList.get(targetIndex));
                }
            }
        });

        animator.start();
    }
}