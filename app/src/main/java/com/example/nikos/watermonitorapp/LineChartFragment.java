package com.example.nikos.watermonitorapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.animation.ChartAnimationListener;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

public class LineChartFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private LineChartView lineChart;
    private LineChartData data;
    private int numOfLines = 1;
    private int maxNumOfLines = 4;
    private int numOfPoints = 12;

    float[][] randomNumbersTab = new float[maxNumOfLines][numOfPoints];

    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLines = true;
    private boolean hasPoints = true;
    private ValueShape shape = ValueShape.CIRCLE;
    private boolean isFilled = false;
    private boolean hasLabels = false;
    private boolean isCubic = false;
    private boolean hasLabelForSelected = false;
    private boolean pointsHaveDifferentColor;

    public LineChartFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static LineChartFragment newInstance(int sectionNumber) {
        LineChartFragment fragment = new LineChartFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_line_chart, container, false);

        lineChart = (LineChartView) rootView.findViewById(R.id.lineChart);
        lineChart.setOnValueTouchListener(new ValueTouchListener());

        // Generate some random values.
        generateValues();

        generateData();

        // Disable viewport recalculations
        lineChart.setViewportCalculationEnabled(false);

        resetViewport();

        return rootView;
    }

    // menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_line_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_reset:
                reset();
                generateData();
                return true;

            case R.id.action_add_line:
                addLineToData();
                return true;

            case R.id.action_toggle_lines:
                toggleLines();
                return true;

            case R.id.action_toggle_points:
                togglePoints();
                return true;

            case R.id.action_toggle_cubic:
                toggleCubic();
                return true;

            case R.id.action_toggle_area:
                toggleFilled();
                return true;

            case R.id.action_point_color:
                togglePointColor();
                return true;

            case R.id.action_shape_circles:
                setCircles();
                return true;

            case R.id.action_shape_square:
                setSquares();
                return true;

            case R.id.action_shape_diamond:
                setDiamonds();
                return true;

            case R.id.action_toggle_labels:
                toggleLabels();
                return true;

            case R.id.action_toggle_axes:
                toggleAxes();
                return true;

            case R.id.action_toggle_axes_names:
                toggleAxesNames();
                return true;

            case R.id.action_animate:
                prepareDataAnimation();
                lineChart.startDataAnimation();
                return true;

            case R.id.action_toggle_selection_mode:
                toggleLabelForSelected();
                Toast.makeText(getActivity(),
                        "Selection mode set to " + lineChart.isValueSelectionEnabled() +
                                " select any point.",Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_toggle_touch_zoom:
                lineChart.setZoomEnabled(!lineChart.isZoomEnabled());
                Toast.makeText(getActivity(), "IsZoomEnabled " + lineChart.isZoomEnabled(),
                        Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_zoom_both:
                lineChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
                return true;

            case R.id.action_zoom_horizontal:
                lineChart.setZoomType(ZoomType.HORIZONTAL);
                return true;

            case R.id.action_zoom_vertical:
                lineChart.setZoomType(ZoomType.VERTICAL);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Generate random values
     */
    private void generateValues() {
        for (int i = 0; i < maxNumOfLines; ++i) {
            for (int j = 0; j < numOfPoints; ++j) {
                randomNumbersTab[i][j] = (float) Math.random() * 100f;
            }
        }
    }

    /**
     * resets the lineChart
     */
    private void reset() {
        numOfLines = 1;

        hasAxes = true;
        hasAxesNames = true;
        hasLines = true;
        hasPoints = true;
        shape = ValueShape.CIRCLE;
        isFilled = false;
        hasLabels = false;
        isCubic = false;
        hasLabelForSelected = false;
        pointsHaveDifferentColor = false;

        lineChart.setValueSelectionEnabled(hasLabelForSelected);
        resetViewport();
    }

    /**
     * resets the lineChart's viewport
     */
    private void resetViewport() {
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.bottom = 0;
        v.top = 100;
        v.left = 0;
        v.right = numOfPoints - 1;
        lineChart.setMaximumViewport(v);
        lineChart.setCurrentViewport(v);
    }

    /**
     * Generate the line lineChart with the given graphic preferences and data.
     */
    private void generateData() {

        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < numOfLines; ++i) {

            List<PointValue> values = new ArrayList<>();
            for (int j = 0; j < numOfPoints; ++j) {
                values.add(new PointValue(j, randomNumbersTab[i][j]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLORS[i]);
            line.setShape(shape);
            line.setCubic(isCubic);
            line.setFilled(isFilled);
            line.setHasLabels(hasLabels);
            line.setHasLabelsOnlyForSelected(hasLabelForSelected);
            line.setHasLines(hasLines);
            line.setHasPoints(hasPoints);
            if (pointsHaveDifferentColor){
                line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
            }
            lines.add(line);
        }

        data = new LineChartData(lines);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setName("Axis X");
                axisY.setName("Axis Y");
            }
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        lineChart.setLineChartData(data);

    }

    /**
     * Adds line to data and reset.
     */
    private void addLineToData() {
        if (data.getLines().size() >= maxNumOfLines) {
            Toast.makeText(getActivity(), "Samples app uses max 4 lines!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            ++numOfLines;
        }

        generateData();
    }

    /**
     * toggle lines on/off
     */
    private void toggleLines() {
        hasLines = !hasLines;

        generateData();
    }

    /**
     * toggle points on/off
     */
    private void togglePoints() {
        hasPoints = !hasPoints;

        generateData();
    }

    /**
     * toggle cubic curve of lineChart on/off
     */
    private void toggleCubic() {
        isCubic = !isCubic;

        generateData();

        if (isCubic) {
            final Viewport v = new Viewport(lineChart.getMaximumViewport());
            v.bottom = -5;//bot bound is 0
            v.top = 105;//top bound is 100

            lineChart.setMaximumViewport(v);

            lineChart.setCurrentViewportWithAnimation(v);
        } else {
            // If not cubic restore viewport to (0,100) range.
            final Viewport v = new Viewport(lineChart.getMaximumViewport());
            v.bottom = 0;
            v.top = 100;

            lineChart.setViewportAnimationListener(new ChartAnimationListener() {

                @Override
                public void onAnimationStarted() {

                }

                @Override
                public void onAnimationFinished() {
                    // Set max viewport and remove listener.
                    lineChart.setMaximumViewport(v);
                    lineChart.setViewportAnimationListener(null);

                }
            });

            lineChart.setCurrentViewportWithAnimation(v);
        }

    }

    /**
     * toggle filling on/off
     */
    private void toggleFilled() {
        isFilled = !isFilled;

        generateData();
    }

    /**
     * toggle point color on/off
     */
    private void togglePointColor() {
        pointsHaveDifferentColor = !pointsHaveDifferentColor;

        generateData();
    }

    /**
     * make circle-like points
     */
    private void setCircles() {
        shape = ValueShape.CIRCLE;

        generateData();
    }

    /**
     * make square-like points
     */
    private void setSquares() {
        shape = ValueShape.SQUARE;

        generateData();
    }

    /**
     * make diamond-like points
     */
    private void setDiamonds() {
        shape = ValueShape.DIAMOND;

        generateData();
    }

    /**
     * toggle Labels on/off
     */
    private void toggleLabels() {
        hasLabels = !hasLabels;

        if (hasLabels) {
            hasLabelForSelected = false;
            lineChart.setValueSelectionEnabled(hasLabelForSelected);
        }

        generateData();
    }

    /**
     * toggle label for selected on/off
     */
    private void toggleLabelForSelected() {
        hasLabelForSelected = !hasLabelForSelected;

        lineChart.setValueSelectionEnabled(hasLabelForSelected);

        if (hasLabelForSelected) {
            hasLabels = false;
        }

        generateData();
    }

    /**
     * toggle axes on/off
     */
    private void toggleAxes() {
        hasAxes = !hasAxes;

        generateData();
    }

    /**
     * toggle axes' names on/off
     */
    private void toggleAxesNames() {
        hasAxesNames = !hasAxesNames;

        generateData();
    }

    /**
     * random reanimation of the graph changing only y values
     */
    private void prepareDataAnimation() {
        for (Line line : data.getLines()) {
            for (PointValue value : line.getValues()) {
                //modify y values
                value.setTarget(value.getX(), (float) Math.random() * 100);
            }
        }
    }

    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {

        }

    }
}
