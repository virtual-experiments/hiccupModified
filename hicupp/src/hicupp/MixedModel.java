package hicupp;

public class MixedModel
{
	public double[] values;
	public int mixcomp;
	public double[][] postprobcons;
	public double[] mixprob;
	public double[] mugg;
	public double[] covgg;
	public double aic;
	public double certainty;

	private static final double criter = 0.001f;
	private static final int iter = 500;
	private static final int pp = 1;
	
	public MixedModel(int mixcomp, double[] values) {
		this.values = values;
		this.mixcomp = mixcomp;
		
		postprobcons = new double[values.length][mixcomp];
		for (int i = 0; i < values.length; i++) {
			double sum = 0f;
			double[] postprobcons_i = postprobcons[i];
			for (int j = 0; j < mixcomp; j++) {
				double x = Math.random();
				postprobcons_i[j] = x;
				sum += x;
			}
			for (int j = 0; j < mixcomp; j++)
				postprobcons_i[j] /= sum;
		}
		mixprob = new double[mixcomp];
		mugg = new double[mixcomp];
		covgg = new double[mixcomp];
		double[][] densitygg = new double[values.length][mixcomp];
		
		double oldaic = 0;
		
		for (int k = 1; k <= iter; k++) {
			for (int j = 0; j < mixcomp; j++) {
				double sum = 0;
				for (int i = 0; i < values.length; i++)
					sum += postprobcons[i][j];
				sum /= values.length;
				mixprob[j] = sum;
			}
					
			for (int j = 0; j < mixcomp; j++) {
				double sum = 0;
				for (int i = 0; i < values.length; i++) {
					sum += values[i] * postprobcons[i][j];
				}
				sum /= values.length * mixprob[j];
				mugg[j] = sum;
			}
					
			for (int j = 0; j < mixcomp; j++) {
				double sum = 0;
				for (int i = 0; i < values.length; i++) {
					double facmin = values[i] - mugg[j];
					sum += postprobcons[i][j] * facmin * facmin;
				}
				sum /= values.length * mixprob[j];
				covgg[j] = sum;
			}

			double sqrt2pi = Math.sqrt(2 * Math.PI);
			for (int i = 0; i < values.length; i++) {
				double[] densitygg_i = densitygg[i];
				for (int j = 0; j < mixcomp; j++) {
					double delta = values[i] - mugg[j];
					densitygg_i[j] = Math.exp(-delta * delta / 2 / covgg[j]) / sqrt2pi / Math.sqrt(covgg[j]);
				}
			}
					
			double loglik = 0;
			for (int j = 0; j < mixcomp; j++) {
				double x = Math.log(mixprob[j]);
				if (!Double.isInfinite(x)) {
					for (int i = 0; i < values.length; i++) {
						double logdensity = Math.log(densitygg[i][j]);
						if (!Double.isInfinite(logdensity)) {
							loglik += postprobcons[i][j] * (x + logdensity);
						}
					}
				}
			}

			aic = -2 * loglik + 2 * (mixcomp + mixcomp - 1 + (mixcomp * (pp + (pp * (pp - 1) / 2))));

			for (int i = 0; i < values.length; i++) {
				double sum = 0;
				double[] densitygg_i = densitygg[i];
				for (int j = 0; j < mixcomp; j++)
					sum += densitygg_i[j];
				double[] postprobcons_i = postprobcons[i];
				for (int j = 0; j < mixcomp; j++)
					postprobcons_i[j] = densitygg_i[j] / sum;
			}

			if (k > 1 && Math.abs(aic - oldaic) < criter)
				break;

			oldaic = aic;
			
			dump();
		}
		
		for (int i = 0; i < values.length; i++) {
			double max = 0;
			double[] postprobcons_i = postprobcons[i];
			for (int j = 0; j < mixcomp; j++)
				if (postprobcons_i[j] > max)
					max = postprobcons_i[j];
			certainty += max;
		}
	}
	
	public void dump() {
		System.out.println("certainty: " + certainty);
		for (int j = 0; j < mixcomp; j++) {
			System.out.println("Component " + j + ": mixprob = " + mixprob[j] + "; mean = " + mugg[j] + "; variance = " + covgg[j]);
		}
		System.out.println();
	}
	
	public static MixedModel iterate(int mixcomp, double[] values, int n) {
		MixedModel best = new MixedModel(mixcomp, values);
		for (int i = 1; i < n; i++) {
			MixedModel model = new MixedModel(mixcomp, values);
			if (model.aic < best.aic)
				best = model;
		}
		return best;
	}
}
