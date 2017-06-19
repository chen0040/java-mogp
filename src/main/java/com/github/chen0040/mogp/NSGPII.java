package com.github.chen0040.mogp;


import com.github.chen0040.data.utils.TupleTwo;
import com.github.chen0040.gp.commons.Observation;
import com.github.chen0040.gp.treegp.TreeGP;
import com.github.chen0040.gp.treegp.gp.*;
import com.github.chen0040.gp.treegp.program.OperatorSet;
import com.github.chen0040.gp.treegp.program.operators.*;
import com.github.chen0040.moea.components.*;
import com.github.chen0040.moea.components.Population;
import com.github.chen0040.moea.components.Solution;
import com.github.chen0040.moea.enums.ReplacementType;
import com.github.chen0040.moea.utils.InvertedCompareUtils;
import com.github.chen0040.moea.utils.TournamentSelection;
import com.github.chen0040.moea.utils.TournamentSelectionResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by xschen on 17/6/2017.
 * NSGA-II
 */
@Getter
@Setter
public class NSGPII {


   private TreeGP gpConfig = new TreeGP();
   private Mediator moeaConfig = new Mediator();

   private int displayEvery = -1;

   @Setter(AccessLevel.NONE)
   private NondominatedPopulation archive = new NondominatedPopulation();

   @Setter(AccessLevel.NONE)
   protected int currentGeneration = 0;

   private RandomGeneratorImpl randomGenerator = new RandomGeneratorImpl();

   private int maxGenerations = 100;
   private double crossoverRate = 0.5;
   private double microMutationRate = 0.1;
   private double macroMutationRate = 0.1;
   private int populationSize = 100;
   private int maxArchive = 50;
   private CostFunction costFunction;
   private int objectiveCount = 2;

   private ReplacementType replacementType = ReplacementType.Generational;

   @Setter(AccessLevel.NONE)
   protected NondominatedSortingPopulation population = new NondominatedSortingPopulation();

   public NondominatedPopulation solve(){
      initialize();
      int maxGenerations = this.getMaxGenerations();
      for(int generation = 0; generation < maxGenerations; ++generation) {
         evolve();
         if(displayEvery > 0 && generation % displayEvery == 0){
            System.out.println("Generation #" + generation + "\tArchive size: " + archive.size());
         }
      }

      return archive;
   }

   public void defaultConfig(){
      gpConfig.getOperatorSet().addAll(new Plus(), new Minus(), new Divide(), new Multiply(), new Power());
      gpConfig.getOperatorSet().addIfLessThanOperator();
      gpConfig.addConstants(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);
      gpConfig.setVariableCount(2); // equal to the number of input parameter in an observation
   }

   public List<Observation> getObservations(){
      return gpConfig.getObservations();
   }

   public OperatorSet getOperatorSet(){
      return gpConfig.getOperatorSet();
   }

   public void addConstants(double... constants) {
      gpConfig.addConstants(constants);
   }

   public void setVariableCount(int variableCount) {
      gpConfig.setVariableCount(variableCount);
   }

   public void initialize(){
      gpConfig.setPopulationSize(getPopulationSize());
      gpConfig.setMaxGeneration(getMaxGenerations());
      gpConfig.setCrossoverRate(crossoverRate);
      gpConfig.setMacroMutationRate(macroMutationRate);
      gpConfig.setMicroMutationRate(microMutationRate);
      gpConfig.setCostEvaluator(null);
      gpConfig.setRandEngine(randomGenerator);

      moeaConfig.setPopulationSize(populationSize);
      moeaConfig.setMaxGenerations(getMaxGenerations());
      moeaConfig.setCrossoverRate(crossoverRate);
      moeaConfig.setMutationRate(macroMutationRate);
      moeaConfig.setCostFunction(null);
      moeaConfig.setRandomGenerator(randomGenerator);
      moeaConfig.setMaxArchive(maxArchive);
      moeaConfig.setReplacementType(replacementType);
      moeaConfig.setObjectiveCount(objectiveCount);

      archive.setMediator(moeaConfig);
      archive.clear();

      population.setMediator(moeaConfig);
      population.initialize(MOOGPSolution::new);

      List<com.github.chen0040.gp.treegp.program.Solution> programs = population.getSolutions().stream().map(s -> {
         MOOGPSolution ms = (MOOGPSolution)s;
         return ms.getGp();
      }).collect(Collectors.toList());

      PopulationInitialization.apply(programs, gpConfig);

      for(int i=0; i < population.size(); ++i){
         evaluate(population.get(i));
      }
      population.sort();
      currentGeneration = 0;
   }

   public void evaluate(Solution obj) {
      MOOGPSolution s = (MOOGPSolution)obj;

      com.github.chen0040.gp.treegp.program.Solution forest = s.getGp().makeCopy();

      List<Double> costs = costFunction.evaluateCosts(forest, moeaConfig.getObjectiveCount(), gpConfig);

      s.getCosts().clear();
      s.getCosts().addAll(costs);

      boolean is_archivable = archive.add(s);

      if (archive.size() > this.getMaxArchive())
      {
         archive.truncate(this.getMaxArchive());
      }
   }




   public void evolve(){

      int iPopSize = this.gpConfig.getPopulationSize();

      int elite_count = (int)(gpConfig.getElitismRatio() * iPopSize);

      int crossover_count = (int)(this.gpConfig.getCrossoverRate() * iPopSize);

      if (crossover_count % 2 != 0) crossover_count += 1;

      int micro_mutation_count = (int)(this.gpConfig.getMicroMutationRate() * iPopSize);
      int macro_mutation_count = (int)(this.gpConfig.getMacroMutationRate() * iPopSize);
      int reproduction_count = iPopSize - crossover_count - micro_mutation_count - macro_mutation_count;



      List<Solution> offspring = new ArrayList<>();


      //do crossover
      for (int offspring_index = 0; offspring_index < crossover_count; offspring_index += 2)
      {
         TupleTwo<Solution, Solution> tournament_winners = tournament();

         MOOGPSolution child1 = (MOOGPSolution)tournament_winners._1().makeCopy();
         MOOGPSolution child2 = (MOOGPSolution)tournament_winners._2().makeCopy();

         com.github.chen0040.gp.treegp.gp.Crossover.apply(child1.getGp(), child2.getGp(), gpConfig);

         offspring.add(child1);
         offspring.add(child2);
      }

      // do point mutation
      for (int offspring_index = 0; offspring_index < micro_mutation_count; ++offspring_index)
      {

         TupleTwo<Solution, Solution> tournament_winners = tournament();

         MOOGPSolution child = (MOOGPSolution)tournament_winners._1().makeCopy();

         MicroMutation.apply(child.getGp(), gpConfig);

         offspring.add(child);
      }

      // do subtree mutation
      for (int offspring_index = 0; offspring_index < macro_mutation_count; ++offspring_index)
      {
         TupleTwo<Solution, Solution> tournament_winners = tournament();

         MOOGPSolution child = (MOOGPSolution)tournament_winners._1().makeCopy();

         MacroMutation.apply(child.getGp(), gpConfig);

         offspring.add(child);

      }

      // do reproduction
      for (int offspring_index = 0; offspring_index < reproduction_count; ++offspring_index)
      {
         TupleTwo<Solution, Solution> tournament_winners = tournament();

         MOOGPSolution child = (MOOGPSolution)tournament_winners._1().makeCopy();

         offspring.add(child);
      }

      for (int i = 0; i < iPopSize; ++i)
      {
         Solution s = offspring.get(i);
         evaluate(s);
      }

      ReplacementType replacementType = getReplacementType();
      if(replacementType == ReplacementType.Generational) {
         merge1(offspring);
      } else if(replacementType == ReplacementType.Tournament) {
         merge2(offspring);
      }
   }

   private TupleTwo<Solution, Solution> tournament(){
      TournamentSelectionResult<Solution> tournament = TournamentSelection.select(population.getSolutions(), getRandomGenerator(), (s1, s2) ->
      {
         int flag;
         if ((flag = InvertedCompareUtils.ConstraintCompare(s1, s2))==0) // return -1 if s1 is better
         {
            if ((flag = InvertedCompareUtils.ParetoObjectiveCompare(s1, s2)) == 0) // return -1 if s1 is better
            {
               flag = InvertedCompareUtils.CrowdingDistanceCompare(s1, s2); // return -1 if s1 is better
            }
         }

         return flag < 0; // return -1 if s1 is better
      });

      return tournament.getWinners();
   }


   protected void merge2(List<Solution> children)
   {
      int populationSize = this.getPopulationSize();

      Population offspring = new Population();

      for (int i = 0; i < populationSize; i++)
      {
         Solution s1 = children.get(i);
         Solution s2 = population.get(i);
         int flag = 0;
         if ((flag = InvertedCompareUtils.ConstraintCompare(s1, s2)) == 0)
         {
            if ((flag = InvertedCompareUtils.ParetoObjectiveCompare(s1, s2)) == 0)
            {
               flag = InvertedCompareUtils.CrowdingDistanceCompare(s1, s2);
            }
         }

         if (flag < 0)
         {
            offspring.add(children.get(i));
         }
         else if (flag > 0)
         {
            offspring.add(children.get(i));
         }
         else
         {
            offspring.add(children.get(i));
            offspring.add(population.get(i));
         }
      }

      population.clear();

      population.add(offspring);

      population.prune(populationSize);
   }

   protected void merge1(List<Solution> children)
   {
      int populationSize = this.getPopulationSize();

      for(Solution solution : children) {
         population.add(solution);
      }

      population.truncate(populationSize);
   }
}
