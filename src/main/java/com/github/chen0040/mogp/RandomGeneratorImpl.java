package com.github.chen0040.mogp;


import com.github.chen0040.gp.services.RandEngine;
import com.github.chen0040.moea.components.RandomGenerator;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Random;


/**
 * Created by xschen on 19/6/2017.
 */
public class RandomGeneratorImpl implements RandomGenerator, RandEngine {

   private Random random = new Random();

   @Override public double uniform() {
      return random.nextDouble();
   }


   @Override public double normal(double mean, double sd) {
      NormalDistribution distribution = new NormalDistribution(mean, sd);
      return distribution.sample();
   }


   @Override public int nextInt(int lower, int upper) {
      return random.nextInt(upper - lower) + lower;
   }


   @Override public double nextDouble() {
      return random.nextDouble();
   }


   @Override public int nextInt(int upper) {
      return random.nextInt(upper);
   }
}
