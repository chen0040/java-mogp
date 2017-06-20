package com.github.chen0040.mogp;


import com.github.chen0040.data.utils.TupleTwo;
import com.github.chen0040.gp.commons.Observation;
import com.github.chen0040.gp.services.Tutorials;
import com.github.chen0040.gp.treegp.TreeGP;
import com.github.chen0040.gp.treegp.program.Solution;
import com.github.chen0040.gp.utils.CollectionUtils;
import com.github.chen0040.moea.algorithms.NSGAII;
import com.github.chen0040.moea.components.NondominatedPopulation;
import com.github.chen0040.moea.enums.ReplacementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;


/**
 * Created by xschen on 20/6/2017.
 */
public class NSGPIIUnitTest {

   private static final Logger logger = LoggerFactory.getLogger(NSGPIIUnitTest.class);



   @Test
   public void test_symbolic_regression() {

      List<Observation> data = Tutorials.mexican_hat();
      CollectionUtils.shuffle(data);
      TupleTwo<List<Observation>, List<Observation>> split_data = CollectionUtils.split(data, 0.9);
      List<Observation> trainingData = split_data._1();
      List<Observation> testingData = split_data._2();

      NSGPII tgp = NSGPII.defaultConfig();
      tgp.setVariableCount(2); // the number of variables is equal to the input dimension of an observation in the "data" list
      tgp.setCostFunction((CostFunction) (solution, gpConfig) -> {
         List<Observation> observations = gpConfig.getObservations();
         double error = 0;
         for(Observation observation : observations){
            solution.execute(observation);
            error += Math.pow(observation.getOutput(0) - observation.getPredictedOutput(0), 2.0);
         }

         double cost1 = error;
         double cost2 = solution.averageTreeDepth();

         return Arrays.asList(cost1, cost2);
      });
      tgp.setMaxGenerations(50);
      tgp.setPopulationSize(500);

      tgp.setDisplayEvery(2);
      NondominatedPopulation pareto_front = tgp.fit(trainingData);
      //logger.info("global: {}", program.mathExpression());
      System.out.println("pareto_front: " + pareto_front.size());

      //test(program, testingData, false);

      MOOGPSolution solution = (MOOGPSolution)pareto_front.get(0);
      Solution program = solution.getGp();



   }

   @Test
   public void test_symbolic_regression_replacement_tournament() {

      List<Observation> data = Tutorials.mexican_hat();
      CollectionUtils.shuffle(data);
      TupleTwo<List<Observation>, List<Observation>> split_data = CollectionUtils.split(data, 0.9);
      List<Observation> trainingData = split_data._1();
      List<Observation> testingData = split_data._2();

      NSGPII tgp = NSGPII.defaultConfig();
      tgp.setVariableCount(2); // the number of variables is equal to the input dimension of an observation in the "data" list
      tgp.setCostFunction((CostFunction) (solution, gpConfig) -> {
         List<Observation> observations = gpConfig.getObservations();
         double error = 0;
         for(Observation observation : observations){
            solution.execute(observation);
            error += Math.pow(observation.getOutput(0) - observation.getPredictedOutput(0), 2.0);
         }

         double cost1 = error;
         double cost2 = solution.averageTreeDepth();

         return Arrays.asList(cost1, cost2);
      });
      tgp.setMaxGenerations(30);
      tgp.setPopulationSize(100);
      tgp.setReplacementType(ReplacementType.Tournament);

      tgp.setDisplayEvery(2);
      NondominatedPopulation pareto_front = tgp.fit(trainingData);
      //logger.info("global: {}", program.mathExpression());
      System.out.println("pareto_front: " + pareto_front.size());

      //test(program, testingData, false);

   }

   private void test(Solution program, List<Observation> testingData, boolean silent) {
      for(Observation observation : testingData) {
         program.execute(observation);
         double predicted = observation.getPredictedOutput(0);
         double actual = observation.getOutput(0);

         if(!silent) {
            logger.info("predicted: {}\tactual: {}", predicted, actual);
         }
      }
   }
}
