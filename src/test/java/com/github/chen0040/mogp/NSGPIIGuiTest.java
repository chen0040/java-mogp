package com.github.chen0040.mogp;


import com.github.chen0040.data.utils.TupleTwo;
import com.github.chen0040.gp.commons.Observation;
import com.github.chen0040.gp.services.Tutorials;
import com.github.chen0040.gp.treegp.program.Solution;
import com.github.chen0040.gp.utils.CollectionUtils;
import com.github.chen0040.moea.components.NondominatedPopulation;
import com.github.chen0040.moea.enums.ReplacementType;
import com.github.chen0040.plt.ParetoFront;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;


/**
 * Created by xschen on 20/6/2017.
 */
public class NSGPIIGuiTest {

   public static void main(String[] args) {

      List<Observation> data = Tutorials.mexican_hat();
      CollectionUtils.shuffle(data);
      TupleTwo<List<Observation>, List<Observation>> split_data = CollectionUtils.split(data, 0.9);
      List<Observation> trainingData = split_data._1();
      List<Observation> testingData = split_data._2();

      NSGPII tgp = NSGPII.defaultConfig();
      tgp.setVariableCount(2); // the number of variables is equal to the input dimension of an observation in the "data" list
      tgp.setCostFunction((CostFunction) (solution, objectiveCount, gpConfig) -> {
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
      tgp.setMaxGenerations(100);
      tgp.setPopulationSize(1000);

      tgp.setDisplayEvery(2);
      NondominatedPopulation pareto_front = tgp.fit(trainingData);
      //logger.info("global: {}", program.mathExpression());
      System.out.println("pareto_front: " + pareto_front.size());

      //test(program, testingData, false);

      List<TupleTwo<Double, Double>> pareto_front_data = pareto_front.front2D();

      ParetoFront chart = new ParetoFront(pareto_front_data, "Pareto Front for MO-GP");
      chart.showIt(true);



   }
}
