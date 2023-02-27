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

import {
    AnimationTriggerMetadata,
    animate,
    state,
    style,
    transition,
    trigger
} from "@angular/animations";

export const animations: Array<AnimationTriggerMetadata> = [
    trigger("flyInOut", [
        state("in", style({ transform: "translateX(0)" })),
        transition("void => *", [
            style({ transform: "translateX(100%)" }),
            animate(1000)
        ]),
        state("out", style({ transform: "translateX(100%)" })),
        transition("* => void", [
            animate(1000, style({ transform: "translateX(100%)" }))
        ])
    ]),

    trigger("fadeInOut", [
        state("out",
            style({ opacity: 0 })
        ),
        state("in",
            style({ opacity: 1 })
        ),
        transition("out => in", animate("2000ms"))
    ])
];
