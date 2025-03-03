import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-submit-button',
  templateUrl: './submit-button.component.html',
  styleUrls: ['./submit-button.component.css']
})
export class SubmitButtonComponent implements OnInit {

  @Input() buttonCaption: string;
  @Input() onClick: () => void;
  @Input() requestState: string = 'none';
  @Input() disabled: boolean = false;

  constructor() {}

  callCallbackFunction() {
    this.onClick();
  }

  ngOnInit(): void {}
}
