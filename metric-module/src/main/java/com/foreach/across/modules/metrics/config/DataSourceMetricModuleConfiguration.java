/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.across.modules.metrics.config;

import com.foreach.across.core.annotations.AcrossCondition;
import com.foreach.across.modules.metrics.AcrossMetric;
import org.springframework.context.annotation.Configuration;

@Configuration
@AcrossCondition("settings.metricsDataSourceEnabled")
public class DataSourceMetricModuleConfiguration extends BaseMetricModuleConfiguration
{
	//TODO: for now it's up to the user to setMetricRegistry on the DataSource, maybe later we can inject the DataSources MetricRegistry into the DataSource

	@Override
	public AcrossMetric getAcrossMetric() {
		return AcrossMetric.DATASOURCES;
	}
}
