///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, OnChanges, Input, SimpleChange } from "@angular/core";

@Component({
    selector: "password-strength-bar",
    templateUrl: "./password-strength-bar.component.html",
    styleUrls: ["./password-strength-bar.component.css"]
})
export class PasswordStrengthBarComponent implements OnChanges {

    @Input() passwordToCheck: string;
    bar0: string;
    bar1: string;
    bar2: string;
    bar3: string;
    bar4: string;

    private colors = ["#F00", "#F90", "#FF0", "#9F0", "#0F0"];

    private static measureStrength(pass: string) {
        let score = 0;
        // award every unique letter until 5 repetitions
        let letters = {};
        for (let i = 0; i < pass.length; i++) {
            letters[pass[i]] = (letters[pass[i]] || 0) + 1;
            score += 5.0 / letters[pass[i]];
        }
        // bonus points for mixing it up
        let variations = {
            digits: /\d/.test(pass),
            lower: /[a-z]/.test(pass),
            upper: /[A-Z]/.test(pass),
            nonWords: /\W/.test(pass)
        };

        let variationCount = 0;
        for (let check in variations) {
            variationCount += (variations[check]) ? 1 : 0;
        }
        score += (variationCount - 1) * 10;
        return Math.trunc(score);
    }

    private getColor(score: number) {
        let idx = 0;
        if (score > 90) {
            idx = 4;
        } else if (score > 70) {
            idx = 3;
        } else if (score >= 40) {
            idx = 2;
        } else if (score >= 20) {
            idx = 1;
        }
        return {
            idx: idx + 1,
            col: this.colors[idx]
        };
    }

    ngOnChanges(changes: { [propName: string]: SimpleChange }): void {
        let password = changes["passwordToCheck"].currentValue;
        this.setBarColors(5, "#DDD");
        if (password) {
            let c = this.getColor(PasswordStrengthBarComponent.measureStrength(password));
            this.setBarColors(c.idx, c.col);
        }
    }

    private setBarColors(count, col) {
        for (let _n = 0; _n < count; _n++) {
            this["bar" + _n] = col;
        }
    }

}
