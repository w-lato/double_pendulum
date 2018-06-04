package project;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.Set;

public class DoublePendulumSimulation extends Application{

    private boolean IS_RUNNING = true;

    private static final Duration TRANSLATE_DURATION      = Duration.seconds(0.25);

    // THETA'' (acceleration)
    double d2_T_1 = 0;
    double d2_T_2 = 0;

    // THETA' (velocity)
    double d1_T_1  = 0;
    double d1_T_2  = 0;

    // INIT VALUES OF THETA (angle)
    double T_1   = 0*(Math.PI)/2;
    double T_2   = 2.3*(Math.PI)/2;

    // FIRST PENDULUM (mass and length)
    double M_1 = 1;
    double R_1 = 200;
//    double R_1 = 2;

    // SECOND PENDULUM
    double M_2 = 2;
    double R_2 = 200;
//    double R_2 = 2;


    // PIN POINT COORDINATES
    double PIN_X     = 500;
    double PIN_Y     = 400;

    // CONST COEFFICIENTS
    final double G = 9.81;
    final double C1 =  1 + M_1/M_2;
    final double C2 = C1 * G;
    final double C3 = C1 * R_1;


    // TIMESTEP
    final double dt = 0.1;
    int ctr = 0;

    // CIRCLES WHICH REPRESENTS PENDULUM'S MASSES
    public static Circle CI_1;
    public static Circle CI_2;

    public static Line E_1;
    public static Line E_2;

    public static Pane canvas;

    // LABEL WITH TIME
    Label timeLabel;

    // LABELS WITH ENERGY VALUE
    Label initEnergyLabel;
    Label blueEnergyLabel;
    Label redEnergyLabel;
    
     // INITIAL VALUES OF POTENTIAL ENERGY
    double blueInit;
    double redInit;

    // LABELES DISPLAYING ENERGY DELTAS
    Label redDeltaLabel;
    Label blueDeltaLabel;
    Label totalDelltaLabel;
    
    // STORES 2D COORDS OF PENDULUMS' TRACES (in order to paint only uniq points)
    private Set<Point2D> trace;


    @Override
    public void start(final Stage primaryStage) {

        trace = new HashSet<>();

        canvas = new Pane();
        final Scene scene = new Scene(canvas, 1000, 800);


        primaryStage.setTitle("Double Pendulum Simulation m1= " + String.valueOf( M_1 ) + " m2= " + String.valueOf( M_2 ) + " L1= " +String.valueOf(R_1) + " L2= " + String.valueOf(R_2));
        primaryStage.setScene(scene);
        primaryStage.show();

        // PENDULUMS' CIRCLES INIT
        CI_1 = new Circle(10, Color.BLUE);
        CI_1.setId("CI_1");
        CI_1.setStroke( Color.BLACK );

        CI_1.relocate(100, 100);

        CI_2 = new Circle(10, Color.RED);
        CI_2.setId("CI_2");
        CI_2.relocate(150, 150);
        CI_2.setStroke( Color.BLACK );

        CI_1.toFront();
        CI_2.toFront();

        // PENDULUMS' EDGES INIT
        int x1 = (int)(PIN_X + R_1 * Math.sin(T_1));
        int y1 = (int)(PIN_Y + R_1 * Math.cos(T_1));
        int x2 = x1 + (int)(R_2 * Math.sin(T_2));
        int y2 = y1 + (int)(R_2 * Math.cos(T_2));

        E_1 = new Line();
        E_1.setId("E_1");
        E_1.setStartX( PIN_X );
        E_1.setStartY( PIN_Y );
        E_1.setEndX( x1 );
        E_1.setEndY( y1 );
        E_1.toFront();


        E_2 = new Line();
        E_2.setId("E_2");
        E_2.setStartX( x1 );
        E_2.setStartY( y1 );
        E_2.setEndX( x2 );
        E_2.setEndY( y2 );
        E_2.toFront();

        // TIME LABEL
        timeLabel = new Label("Time: 0 [s]");

        // ENERGY LABELS 
        blueInit = ( M_1 * G * (R_1 - Math.cos( T_1 )) );
        redInit = ( M_2 * G * (R_2 - Math.cos( T_2 )) );

        initEnergyLabel = new Label("Init energy of Blue: " +(int)( blueInit ) + " [J] of Red: " + (int)( redInit ) + " [J]");
        blueEnergyLabel = new Label("");
        redEnergyLabel = new Label("");

        blueDeltaLabel = new Label("");
        redDeltaLabel = new Label("");
        totalDelltaLabel = new Label("");

        // ADD CLEAR BUTTON
        Button clearBtn = new Button("Clear");
        clearBtn.setId("clearBtn");
        clearBtn.setOnAction( new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                System.out.println(" clear pressed");
                if( trace.isEmpty() )return;

                canvas.getChildren().removeIf( node -> node.getId() == null );
                trace.clear();
            }
        });


        // STOP BUTTON
        Button stopBtn = new Button("Stop");
        stopBtn.setId("stopBtn");
        stopBtn.setOnAction( new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if( IS_RUNNING )
                {
                    IS_RUNNING = false;
                    stopBtn.setText("Start");
                }
                else {
                    IS_RUNNING = true;
                    stopBtn.setText("Stop");
                }

            }
        });

        VBox vbox = new VBox(10);
        vbox.setId("vBox");
        vbox.getChildren().addAll( timeLabel, initEnergyLabel, blueEnergyLabel, redEnergyLabel, blueDeltaLabel, redDeltaLabel, totalDelltaLabel, clearBtn, stopBtn );

        // DISPLAY COMPONENTS OF SCENE
        canvas.getChildren().addAll(CI_1, CI_2, E_1, E_2, vbox);
//        canvas.getChildren().addAll(CI_1, CI_2, E_1, E_2, clearBtn);




        // INFINITE LOOP WHICH ANIMATES PENDULUM
        final Timeline loop = new Timeline(new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {

            double T_DIFF;
            double C_4 ;
            double C_5;
            double C_6;
            double C_7;

            double SIN_T1;
            double SIN_T2;
            double SIN_T_DIFF;
            double COS_T_DIFF;

            double C_8;

            @Override
            public void handle(final ActionEvent t) {

                if( IS_RUNNING )
                {
//                    System.out.println("test");
                    // CONST CALCULATIONS

                    T_DIFF = T_1 - T_2;
                    C_4 = R_2 * d1_T_2 * d1_T_2;
                    C_5 =R_1 * d1_T_1 * d1_T_1;
                    C_6 = C3 * d1_T_1 * d1_T_1;
                    C_7 = R_2 * d1_T_2 * d1_T_2;

                    SIN_T1 = Math.sin(T_1);
                    SIN_T2 = Math.sin(T_2);
                    SIN_T_DIFF = Math.sin(T_DIFF);
                    COS_T_DIFF = Math.cos(T_DIFF);

                    C_8 = C1 - COS_T_DIFF * COS_T_DIFF;

                    // VERSION 1 - WITHOUT CONST VALUES
//                d2_T_1  =  ( G * (Math.sin(T_2)*Math.cos(T_DIFF)- C1 * Math.sin(T_1)) -
//                           (R_2 * d1_T_2 * d1_T_2 + R_1 * d1_T_1 * d1_T_1 * Math.cos(T_DIFF)) * Math.sin(T_DIFF)) /
//                           (R_1 * ( C1 - Math.cos(T_DIFF)*Math.cos(T_DIFF)));
//
//                d2_T_2  =  ( C2 * (Math.sin(T_1)*Math.cos(T_DIFF)-Math.sin(T_2)) +
//                           (C3 * d1_T_1 * d1_T_1 + R_2 * d1_T_2 * d1_T_2 * Math.cos(T_DIFF)) * Math.sin(T_DIFF)) /
//                           (R_2 * (C1 - Math.cos(T_DIFF)*Math.cos(T_DIFF)));

                    // VERSION 2 - MORE CONCISE VERSION
//                d2_T_1  =  ( G * (Math.sin(T_2) * Math.cos(T_DIFF) - C1 * Math.sin(T_1)) -
//                           ( C_4 + C_5 * Math.cos(T_DIFF)) * Math.sin(T_DIFF)) /
//                           (R_1 * ( C1 - Math.cos(T_DIFF)*Math.cos(T_DIFF)));
//
//                d2_T_2  =  ( C2 * (Math.sin(T_1)*Math.cos(T_DIFF) - Math.sin(T_2)) +
//                           ( C_6 + C_7 * Math.cos(T_DIFF)) * Math.sin(T_DIFF)) /
//                           ( R_2 * (C1 - Math.cos(T_DIFF)*Math.cos(T_DIFF)));

                    // VERSION 3 OF ANGULAR ACCELERATION
                    d2_T_1  =  ( G * (SIN_T2 * COS_T_DIFF - C1 * SIN_T1) -
                            ( C_4 + C_5 * COS_T_DIFF) * SIN_T_DIFF) /
                            (R_1 * C_8);

                    d2_T_2  =  ( C2 * (SIN_T1 * COS_T_DIFF - SIN_T2) +
                            ( C_6 + C_7 * COS_T_DIFF) * SIN_T_DIFF) /
                            ( R_2 * C_8);

                    // ANGULAR VELOCITY
                    d1_T_1   += d2_T_1 * dt;
                    d1_T_2   += d2_T_2 * dt;

                    // ANGLE
                    T_1    += d1_T_1 * dt;
                    T_2    += d1_T_2 * dt;


                    int x1 = (int)(PIN_X + R_1 * Math.sin(T_1));
                    int y1 = (int)(PIN_Y + R_1 * Math.cos(T_1));
                    int x2 = x1 + (int)(R_2 * Math.sin(T_2));
                    int y2 = y1 + (int)(R_2 * Math.cos(T_2));

//                    int x1 = (int)(PIN_X + R_1 * Math.sin(T_1) * 100.0);
//                    int y1 = (int)(PIN_Y + R_1 * Math.cos(T_1) * 100.0);
//                    int x2 = x1 + (int)(R_2 * Math.sin(T_2) * 100.0);
//                    int y2 = y1 + (int)(R_2 * Math.cos(T_2) * 100.0);

                    if( trace.add( new Point2D(x1 ,y1)) )
                    {
                        Circle dot = new Circle(x1, y1,1, Color.BLACK);
                        dot.toBack();
                        canvas.getChildren().add(dot);

                        CI_1.toFront();
                        CI_2.toFront();
                    }
                    if( trace.add( new Point2D(x2 ,y2)) )
                    {
                        Circle dot = new Circle(x2, y2,1, Color.DARKRED);
                        dot.toBack();
                        canvas.getChildren().add(dot);

                        CI_1.toFront();
                        CI_2.toFront();
                    }

                    // SET COORDS OF EDGES AND PENDULUMS' MASSES
                    E_1.setEndX( x1 );
                    E_1.setEndY( y1 );

                    E_2.setStartX( x1 );
                    E_2.setStartY( y1 );
                    E_2.setEndX( x2 );
                    E_2.setEndY( y2 );

                    CI_1.setLayoutX( x1 );
                    CI_1.setLayoutY( y1 );

                    CI_2.setLayoutX( x2 );
                    CI_2.setLayoutY( y2 );

                    // INCREASE TIME IN TIMELABEL
                    ctr++;
                    timeLabel.setText("Time: " + String.valueOf( (int)(dt * ctr)  ) + " [s]");
                    
                    // UPDATE ENERGY LABELS
                    blueEnergyLabel.setText( "Blue: " + (int)( 0.5 * M_1 * d1_T_1 * d1_T_1 +   M_1 * G * (R_1 - Math.cos( T_1 )) ) );
                    redEnergyLabel.setText( "Red: " + (int)( 0.5 * M_2 * d2_T_1 * d2_T_1 +   M_2 * G * (R_2 - Math.cos( T_2 )) ) );
                
                     // DELTA LABELS
                    blueDeltaLabel.setText("BLUE dE = " + (int)( blueInit - blueCurrent ) );
                    redDeltaLabel.setText("RED dE   = "  + (int)( redInit - redCurrent ) );
                    totalDelltaLabel.setText("TOTAL dE = " + (int)(blueInit + redInit - blueCurrent - redCurrent ) );
                }
            }
        }));

        loop.setCycleCount(Timeline.INDEFINITE);
        loop.play();
    }




    public static void main(final String[] args) {
        launch(args);
    }
}
