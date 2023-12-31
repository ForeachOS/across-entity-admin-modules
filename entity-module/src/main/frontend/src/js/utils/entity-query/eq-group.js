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
import EQType from "./eq-type";
import {isNullOrUndefined, arrayEquals, convertArgsToArray} from "../utilities";

/**
 * Represents an group of values in an {@link EntityQuery} or {@link EntityQueryCondition}
 * @see com.foreach.across.modules.entity.query.EQGroup
 *
 * @author Steven Gentens
 * @since 2.2.0
 */
class EQGroup extends EQType {
  /**
   * The values may be:
   *  - an array containing the arguments: [arg1, arg2, arg3]
   *  - a single item: arg1
   *  - a set of items listed separately: arg1, arg2, arg3
   * @param values the values of the group.
   */
  constructor( ...values ) {
    super();
    this.values = convertArgsToArray( values );
  }

  getValues() {
    return this.values;
  }

  toString() {
    return `(${this.values.join( "," )})`;
  }

  equals( that ) {
    if ( this.valueOf() === that.valueOf() ) {
      return true;
    }
    if ( isNullOrUndefined( that ) ) {
      return false;
    }
    return arrayEquals( ...this.values, ...that.values );
  }
}

export default EQGroup;
